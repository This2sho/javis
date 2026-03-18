package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationState;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class InMemoryAnswerRepository extends InMemoryRepository<Answer> implements AnswerRepository {

    @Override
    public List<Answer> findAllByQuestionIdIn(List<Association<Question>> questionIds) {
        return findAll(answer -> questionIds.contains(answer.getQuestionId()));
    }

    @Override
    public Optional<Answer> findByQuestionId(Association<Question> questionId) {
        return findOne(answer -> answer.getQuestionId().equals(questionId));
    }

    @Override
    public List<Answer> findAllByEvaluationState(EvaluationState evaluationState) {
        return findAll(answer -> answer.getEvaluationState() == evaluationState);
    }

    @Override
    public int _updateStateAtomic(Long id, EvaluationState next, Set<EvaluationState> allowedStates) {
        Answer answer = findOne(a -> a.getId().equals(id))
                .orElseThrow();
        try {
            answer.validateCanStartScoring();
        } catch (Exception e) {
            return 0;
        }
        return 1;
    }
}
