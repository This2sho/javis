package com.javis.learn_hub.evaluation.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.evaluation.infrastructure.EvaluationClient;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.support.builder.QuestionBuilder;
import com.javis.learn_hub.support.config.WithMockEventPublisher;
import com.javis.learn_hub.support.domain.Association;
import java.util.Set;
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
    private EvaluationService evaluationService;

    @Autowired
    private AnswerProcessor answerProcessor;

    @MockitoBean
    private EvaluationClient evaluationClient;

    @MockitoBean
    private InterviewReader interviewReader;

    @MockitoBean
    private ProblemReader problemReader;

    @DisplayName("동시에 여러 번 재채점 요청이 들어와도 채점 서버 HTTP 요청은 정확히 한 번만 전송된다")
    @Test
    void retryEvaluation_whenConcurrentRequests_sendsEvaluationOnlyOnce() throws InterruptedException {
        // given
        Long questionId = 99999L;

        AnswerCreatedEvent createdEvent = answerProcessor.create(questionId, "테스트 답변");
        answerProcessor.prepareScoring(createdEvent.answerId()); // PENDING → SCORING
        answerProcessor.fail(createdEvent.answerId());         // SCORING → FAILED

        Question question = QuestionBuilder.builder().withInterviewId(1L).withProblemId(1L).build();
        Interview mockInterview = Mockito.mock(Interview.class);
        given(mockInterview.getMemberId()).willReturn(Association.from(1L));
        given(interviewReader.getQuestion(questionId)).willReturn(question);
        given(interviewReader.get(any(Association.class))).willReturn(mockInterview);

        ProblemScoringInfo mockScoringInfo = Mockito.mock(ProblemScoringInfo.class);
        given(mockScoringInfo.getReferenceAnswer()).willReturn("모범 답안");
        given(mockScoringInfo.getKeywordsValue()).willReturn(Set.of("키워드"));
        given(problemReader.getProblemScoringInfo(anyLong())).willReturn(mockScoringInfo);

        // when - 5개 스레드가 동시에 재채점 요청
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    evaluationService.requestEvaluation(questionId);
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

        // then - HTTP 요청은 정확히 1번만
        verify(evaluationClient, times(1)).request(any(), any(), any(), any());
    }
}
