package com.javis.learn_hub.answer.service;

import com.javis.learn_hub.answer.domain.EvaluationResult;
import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.answer.domain.service.Evaluator;
import com.javis.learn_hub.answer.service.dto.AnswerRequest;
import com.javis.learn_hub.answer.service.dto.EvaluationRequest;
import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.interview.domain.service.InterviewFinder;
import com.javis.learn_hub.interview.domain.service.dto.ReferenceView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final Evaluator evaluator;
    private final AnswerProcessor answerProcessor;
    private final InterviewFinder interviewFinder;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 1. 답변을 가지고 채점 진행
     * 2. 채점 결과 저장 및 반영
     */
    public EvaluationResult answer(Long questionId, AnswerRequest request) {
        ReferenceView reference = interviewFinder.findReference(questionId);
        EvaluationRequest evaluationRequest = new EvaluationRequest(request.problem(), reference.referenceAnswer(),
                reference.keywords(), request.userAnswer());
        EvaluationResult evaluationResult = evaluator.evaluate(evaluationRequest);
        List<DomainEvent> events = answerProcessor.create(questionId, request.userAnswer(), evaluationResult);
        events.forEach(applicationEventPublisher::publishEvent);
        return evaluationResult;
    }
}
