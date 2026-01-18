package com.javis.learn_hub.answer.domain.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationResult;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.event.AnswerFinishEvent;
import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.support.domain.Association;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AnswerProcessor {

    private final AnswerRepository answerRepository;

    public List<DomainEvent> create(Long questionId, String userAnswer, EvaluationResult result) {
        Answer answer = new Answer(Association.from(questionId), userAnswer, result);
        answerRepository.save(answer);
        List<DomainEvent> events = new ArrayList<>();
        events.add(new AnswerFinishEvent(questionId));
        return events;
    }
}
