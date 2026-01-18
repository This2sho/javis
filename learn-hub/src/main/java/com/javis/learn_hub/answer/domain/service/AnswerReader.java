package com.javis.learn_hub.answer.domain.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AnswerReader {

    private final AnswerRepository answerRepository;

    public List<Answer> getAll(List<Association<Question>> questionIds) {
        return answerRepository.findAllByQuestionIdIn(questionIds);
    }
}
