package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Optional;

public class InMemoryAnswerRepository extends InMemoryRepository<Answer> implements AnswerRepository {

    @Override
    public List<Answer> findAllByQuestionIdIn(List<Association<Question>> questionIds) {
        return findAll(answer -> questionIds.contains(answer.getQuestionId()));
    }

    @Override
    public Optional<Answer> findByQuestionId(Association<Question> questionId) {
        return findOne(answer -> answer.getQuestionId().equals(questionId));
    }
}
