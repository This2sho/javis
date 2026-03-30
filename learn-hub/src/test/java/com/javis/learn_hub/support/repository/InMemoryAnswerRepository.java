package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationState;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

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
    public List<Answer> findByEvaluationStateInOrderByCreatedAtAsc(List<EvaluationState> evaluationStates, Pageable pageable) {
        return findAll(answer -> evaluationStates.contains(answer.getEvaluationState())).stream()
                .sorted((left, right) -> left.getCreatedAt().compareTo(right.getCreatedAt()))
                .limit(pageable.getPageSize())
                .toList();
    }

    @Override
    public int failStaleScoringAnswers(EvaluationState scoringState, EvaluationState failedState, LocalDateTime cutoff, LocalDateTime now) {
        List<Answer> staleAnswers = findAll(answer -> answer.getEvaluationState() == scoringState
                && answer.getUpdatedAt() != null
                && answer.getUpdatedAt().isBefore(cutoff));
        staleAnswers.forEach(answer -> {
            answer.fail();
            save(answer);
        });
        return staleAnswers.size();
    }
}
