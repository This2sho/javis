package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationState;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
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
    public int _updateStateAtomic(Long id, EvaluationState next, Set<EvaluationState> allowedStates, LocalDateTime now) {
        Answer answer = findOne(a -> a.getId().equals(id))
                .orElseThrow();
        if (!allowedStates.contains(answer.getEvaluationState())) {
            return 0;
        }
        setEvaluationState(answer, next);
        return 1;
    }

    @Override
    public int _failStuckScoringAnswers(EvaluationState failed, EvaluationState scoring, LocalDateTime threshold, LocalDateTime now) {
        List<Answer> stale = findAll(a ->
                a.getEvaluationState() == scoring &&
                a.getCreatedAt() != null &&
                a.getCreatedAt().isBefore(threshold)
        );
        stale.forEach(a -> setEvaluationState(a, failed));
        return stale.size();
    }

    private void setEvaluationState(Answer answer, EvaluationState state) {
        try {
            Field field = Answer.class.getDeclaredField("evaluationState");
            field.setAccessible(true);
            field.set(answer, state);
        } catch (Exception e) {
            throw new IllegalStateException("evaluationState 설정 실패", e);
        }
    }
}
