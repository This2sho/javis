package com.javis.learn_hub.answer.domain.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AnswerProcessor {

    private final AnswerRepository answerRepository;

    public AnswerCreatedEvent create(Long questionId, String userAnswer) {
        Answer answer = Answer.create(questionId, userAnswer);
        answerRepository.save(answer);
        return new AnswerCreatedEvent(answer.getId(), questionId, userAnswer);
    }
}
