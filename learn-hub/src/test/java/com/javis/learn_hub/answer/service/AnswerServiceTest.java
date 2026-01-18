package com.javis.learn_hub.answer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.answer.domain.EvaluationResult;
import com.javis.learn_hub.answer.domain.Grade;
import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.answer.domain.service.Evaluator;
import com.javis.learn_hub.answer.service.dto.AnswerRequest;
import com.javis.learn_hub.answer.service.dto.EvaluationRequest;
import com.javis.learn_hub.event.AnswerFinishEvent;
import com.javis.learn_hub.interview.domain.service.InterviewFinder;
import com.javis.learn_hub.interview.domain.service.dto.ReferenceView;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private Evaluator evaluator;

    @Mock
    private AnswerProcessor answerProcessor;

    @Mock
    private InterviewFinder interviewFinder;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AnswerService answerService;

    @DisplayName("사용자 답변을 채점하고 사용자 답변과 채점 결과를 저장하고 답변 종료 이벤트를 발행한다.")
    @Test
    void testAnswer() {
        //given
        Long questionId = 1L;
        AnswerRequest request = new AnswerRequest(
                "문제",
                "사용자 답변"
        );

        ReferenceView reference = new ReferenceView(
                "정답",
                Set.of("키워드1", "키워드2")
        );

        EvaluationResult evaluationResult =
                new EvaluationResult(Grade.GOOD, "피드백");

        given(interviewFinder.findReference(questionId))
                .willReturn(reference);

        given(answerProcessor.create(
                eq(questionId),
                eq(request.userAnswer()),
                eq(evaluationResult)
        )).willReturn(List.of(new AnswerFinishEvent(questionId)));

        given(evaluator.evaluate(any(EvaluationRequest.class)))
                .willReturn(evaluationResult);

        //when
        EvaluationResult result = answerService.answer(questionId, request);

        //then
        assertThat(result).isSameAs(evaluationResult);
        then(interviewFinder).should().findReference(questionId);
        verify(answerProcessor).create(questionId, request.userAnswer(), evaluationResult);
        verify(applicationEventPublisher).publishEvent(new AnswerFinishEvent(questionId));
    }
}
