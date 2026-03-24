package com.javis.learn_hub.evaluation.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.answer.service.AnswerCommandService;
import com.javis.learn_hub.evaluation.infrastructure.AnswerEvaluator;
import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import com.javis.learn_hub.event.EvaluationRetryEvent;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.support.config.WithMockEventPublisher;
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
    private EvaluationEventListener evaluationEventListener;

    @Autowired
    private AnswerProcessor answerProcessor;

    @MockitoBean
    private AnswerEvaluator answerEvaluator;

    @MockitoBean
    private ProblemReader problemReader;

    @DisplayName("동시에 여러 번 재채점 요청이 들어와도 Gemini 채점 요청은 정확히 한 번만 전송된다")
    @Test
    void retryEvaluation_whenConcurrentRequests_sendsEvaluationOnlyOnce() throws InterruptedException {
        // given
        Long questionId = 99999L;

        AnswerCreatedEvent createdEvent = answerProcessor.create(questionId, "테스트 답변");
        answerProcessor.prepareScoring(createdEvent.questionId()); // PENDING → SCORING
        answerProcessor.fail(createdEvent.answerId());           // SCORING → FAILED

        ProblemScoringInfo mockScoringInfo = Mockito.mock(ProblemScoringInfo.class);
        given(mockScoringInfo.getReferenceAnswer()).willReturn("모범 답안");
        given(problemReader.getProblemScoringInfoByQuestionId(anyLong())).willReturn(mockScoringInfo);
        given(answerEvaluator.evaluate(anyString(), anyString()))
                .willReturn(new EvaluationResponse("GOOD", "잘 답변했습니다."));

        // when - 5개 스레드가 동시에 재채점 요청
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    answerCommandService.prepareScoring(questionId)
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
        verify(answerEvaluator, times(1)).evaluate(any(), any());
    }

    @DisplayName("동시에 여러 번 답변 생성 이벤트가 들어와도 Gemini 채점 요청은 정확히 한 번만 전송된다")
    @Test
    void answerCreatedEvent_whenConcurrentRequests_sendsEvaluationOnlyOnce() throws InterruptedException {
        // given
        Long questionId = 88888L;

        AnswerCreatedEvent createdEvent = answerProcessor.create(questionId, "테스트 답변");

        ProblemScoringInfo mockScoringInfo = Mockito.mock(ProblemScoringInfo.class);
        given(mockScoringInfo.getReferenceAnswer()).willReturn("모범 답안");
        given(problemReader.getProblemScoringInfoByQuestionId(anyLong())).willReturn(mockScoringInfo);
        given(answerEvaluator.evaluate(anyString(), anyString()))
                .willReturn(new EvaluationResponse("GOOD", "잘 답변했습니다."));

        // when
        runConcurrently(5, () -> evaluationEventListener.onAnswerCreated(createdEvent));

        // then
        verify(answerEvaluator, timeout(5000).times(1)).evaluate(anyString(), anyString());
    }

    @DisplayName("동시에 여러 번 재채점 이벤트가 들어와도 Gemini 채점 요청은 정확히 한 번만 전송된다")
    @Test
    void evaluationRetryEvent_whenConcurrentRequests_sendsEvaluationOnlyOnce() throws InterruptedException {
        // given
        Long questionId = 77777L;

        AnswerCreatedEvent createdEvent = answerProcessor.create(questionId, "테스트 답변");
        answerProcessor.prepareScoring(createdEvent.questionId());
        answerProcessor.fail(createdEvent.answerId());

        ProblemScoringInfo mockScoringInfo = Mockito.mock(ProblemScoringInfo.class);
        given(mockScoringInfo.getReferenceAnswer()).willReturn("모범 답안");
        given(problemReader.getProblemScoringInfoByQuestionId(anyLong())).willReturn(mockScoringInfo);
        given(answerEvaluator.evaluate(anyString(), anyString()))
                .willReturn(new EvaluationResponse("GOOD", "잘 답변했습니다."));

        EvaluationRetryEvent retryEvent = new EvaluationRetryEvent(questionId);

        // when
        runConcurrently(5, () -> evaluationEventListener.onEvaluationRetry(retryEvent));

        // then
        verify(answerEvaluator, timeout(5000).times(1)).evaluate(anyString(), anyString());
    }

    private void runConcurrently(int threadCount, ThrowingRunnable action) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    action.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }
}
