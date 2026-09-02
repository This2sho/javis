package com.javis.learn_hub.evaluation.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.answer.service.AnswerCommandService;
import com.javis.learn_hub.evaluation.domain.repository.EvaluationRepository;
import com.javis.learn_hub.evaluation.infrastructure.AnswerEvaluator;
import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.interview.domain.repository.QuestionRepository;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.support.config.WithMockEventPublisher;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WithMockEventPublisher
@SpringBootTest
class EvaluationServiceConcurrencyTest {

    @Autowired
    private AnswerCommandService answerCommandService;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private EvaluationQueuePoller evaluationQueuePoller;

    @Autowired
    private AnswerProcessor answerProcessor;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @MockitoBean
    private AnswerEvaluator answerEvaluator;

    @MockitoBean
    private ProblemReader problemReader;

    @DisplayName("동시에 여러 번 재채점 요청이 들어와도 Gemini 채점 요청은 정확히 한 번만 전송된다")
    @Test
    void retryEvaluation_whenConcurrentRequests_sendsEvaluationOnlyOnce() throws InterruptedException {
        // given
        Long questionId = createQuestion().getId();

        AnswerCreatedEvent createdEvent = answerProcessor.create(questionId, "테스트 답변");
        answerProcessor.prepareScoring(createdEvent.answerId()); // PENDING → SCORING
        answerProcessor.fail(createdEvent.answerId());           // SCORING → FAILED

        ProblemScoringInfo mockScoringInfo = Mockito.mock(ProblemScoringInfo.class);
        given(mockScoringInfo.getReferenceAnswer()).willReturn("모범 답안");
        given(problemReader.getProblemScoringInfoByQuestionId(anyLong())).willReturn(mockScoringInfo);
        given(answerEvaluator.evaluate(anyString(), anyString(), anyString(), anyList(), any()))
                .willReturn(new EvaluationResponse("판단 근거", "GOOD", "잘 답변했습니다.", java.util.List.of(), java.util.List.of()));

        // when - 5개 스레드가 동시에 재채점 요청
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    answerCommandService.prepareScoring(createdEvent.answerId())
                            .ifPresent(answer -> evaluationService.evaluate(answer, questionId));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 동시 시작
        doneLatch.await();
        executor.shutdown();

        // then - Gemini 요청은 정확히 1번만
        verify(answerEvaluator, times(1)).evaluate(anyString(), any(), any(), anyList(), any());
    }

    @DisplayName("동시에 여러 번 답변 생성 이벤트가 들어와도 Gemini 채점 요청은 정확히 한 번만 전송된다")
    @Test
    void answerCreatedEvent_whenConcurrentRequests_sendsEvaluationOnlyOnce() throws InterruptedException {
        // given
        Long questionId = createQuestion().getId();

        AnswerCreatedEvent createdEvent = answerProcessor.create(questionId, "테스트 답변");

        ProblemScoringInfo mockScoringInfo = Mockito.mock(ProblemScoringInfo.class);
        given(mockScoringInfo.getReferenceAnswer()).willReturn("모범 답안");
        given(problemReader.getProblemScoringInfoByQuestionId(anyLong())).willReturn(mockScoringInfo);
        given(answerEvaluator.evaluate(anyString(), anyString(), anyString(), anyList(), any()))
                .willReturn(new EvaluationResponse("판단 근거", "GOOD", "잘 답변했습니다.", java.util.List.of(), java.util.List.of()));

        // when
        runConcurrently(5, evaluationQueuePoller::pollEvaluationQueue);

        // then
        verify(answerEvaluator, timeout(5000).times(1)).evaluate(anyString(), anyString(), anyString(), anyList(), any());
    }

    @DisplayName("동시에 여러 번 재채점 이벤트가 들어와도 Gemini 채점 요청은 정확히 한 번만 전송된다")
    @Test
    void evaluationRetryEvent_whenConcurrentRequests_sendsEvaluationOnlyOnce() throws InterruptedException {
        // given
        Long questionId = createQuestion().getId();

        AnswerCreatedEvent createdEvent = answerProcessor.create(questionId, "테스트 답변");
        answerProcessor.prepareScoring(createdEvent.answerId());
        answerProcessor.fail(createdEvent.answerId());

        ProblemScoringInfo mockScoringInfo = Mockito.mock(ProblemScoringInfo.class);
        given(mockScoringInfo.getReferenceAnswer()).willReturn("모범 답안");
        given(problemReader.getProblemScoringInfoByQuestionId(anyLong())).willReturn(mockScoringInfo);
        given(answerEvaluator.evaluate(anyString(), anyString(), anyString(), anyList(), any()))
                .willReturn(new EvaluationResponse("판단 근거", "GOOD", "잘 답변했습니다.", java.util.List.of(), java.util.List.of()));

        // when
        runConcurrently(5, evaluationQueuePoller::pollEvaluationQueue);

        // then
        verify(answerEvaluator, timeout(5000).times(1)).evaluate(anyString(), anyString(), anyString(), anyList(), any());
    }

    @DisplayName("동시에 여러 번 채점 완료 처리가 들어와도 완료 이벤트와 평가 결과는 하나만 생성된다")
    @Test
    void completeEvaluation_whenConcurrentRequests_completesOnlyOnce() throws InterruptedException {
        // given
        Long questionId = createQuestion().getId();
        AnswerCreatedEvent createdEvent = answerProcessor.create(questionId, "테스트 답변");
        answerProcessor.prepareScoring(createdEvent.answerId());

        EvaluationResponse result = new EvaluationResponse("판단 근거", "GOOD", "잘 답변했습니다.", java.util.List.of(), java.util.List.of());
        AtomicInteger successCount = new AtomicInteger();

        // when
        List<Throwable> errors = runConcurrentlyCollectErrors(
                5,
                () -> {
                    evaluationService.completeEvaluation(createdEvent.answerId(), questionId, result);
                    successCount.incrementAndGet();
                }
        );

        // then
        assertThat(evaluationRepository.findByAnswerId(Association.from(createdEvent.answerId()))).isPresent();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(errors).hasSize(4);
    }

    private void runConcurrently(int threadCount, ThrowingRunnable action) throws InterruptedException {
        List<Throwable> errors = runConcurrentlyCollectErrors(threadCount, action);
        if (!errors.isEmpty()) {
            throw new AssertionError("동시 실행 중 예외 발생", errors.get(0));
        }
    }

    private List<Throwable> runConcurrentlyCollectErrors(int threadCount, ThrowingRunnable action) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    action.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        return errors;
    }

    private Question createQuestion() {
        Interview interview = interviewRepository.save(InterviewBuilder.builder().build());
        return questionRepository.save(
                Question.rootQuestionOf(Association.from(1L), Association.from(interview.getId()), 0, "기본 질문입니다.")
        );
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }
}
