package com.javis.learn_hub.answer.service;

import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.answer.service.dto.AnswerRequest;
import com.javis.learn_hub.answer.service.dto.AnswerSubmitResponse;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AnswerCommandService {

    private final AnswerProcessor answerProcessor;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public AnswerSubmitResponse submitAnswer(Long questionId, AnswerRequest request) {
        AnswerCreatedEvent event = answerProcessor.create(questionId, request.userAnswer());
        applicationEventPublisher.publishEvent(event);
        return AnswerSubmitResponse.accepted(event.answerId());
    }
}
