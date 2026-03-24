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

    public Answer get(Long answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답변입니다: " + answerId));
    }

    public List<Answer> getAll(List<Association<Question>> questionIds) {
        return answerRepository.findAllByQuestionIdIn(questionIds);
    }

    public Answer getByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(Association.from(questionId))
                .orElseThrow(() -> new IllegalArgumentException("해당 질문에 대한 답변이 존재하지 않습니다: " + questionId));
    }
}
