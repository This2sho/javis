package com.javis.learn_hub.answer.domain.repository;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationState;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends Repository<Answer, Long> {

    Answer save(Answer answer);

    Optional<Answer> findById(Long id);

    List<Answer> findAllByQuestionIdIn(List<Association<Question>> questionIds);

    Optional<Answer> findByQuestionId(Association<Question> questionId);

    List<Answer> findAllByEvaluationState(EvaluationState evaluationState);

    // 내부용
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Answer a SET a.evaluationState = :next, a.updatedAt = :now " +
            "WHERE a.id = :id AND a.evaluationState IN :allowedStates")
    int _updateStateAtomic(@Param("id") Long id,
                           @Param("next") EvaluationState next,
                           @Param("allowedStates") Set<EvaluationState> allowedStates,
                           @Param("now") LocalDateTime now);

    default boolean startScoring(Long id) {
        return _updateStateAtomic(id, EvaluationState.SCORING, Set.of(EvaluationState.PENDING, EvaluationState.FAILED), LocalDateTime.now()) == 1;
    }

    default boolean completeScoring(Long id) {
        return _updateStateAtomic(id, EvaluationState.SCORED, Set.of(EvaluationState.SCORING), LocalDateTime.now()) == 1;
    }

    default boolean failScoring(Long id) {
        return _updateStateAtomic(id, EvaluationState.FAILED, Set.of(EvaluationState.SCORING), LocalDateTime.now()) == 1;
    }

    // 내부용
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Answer a SET a.evaluationState = :failed, a.updatedAt = :now " +
            "WHERE a.evaluationState = :scoring AND a.updatedAt < :threshold")
    int _failStuckScoringAnswers(@Param("failed") EvaluationState failed,
                                 @Param("scoring") EvaluationState scoring,
                                 @Param("threshold") LocalDateTime threshold,
                                 @Param("now") LocalDateTime now);

    default int failStuckScoringAnswers(LocalDateTime threshold) {
        return _failStuckScoringAnswers(EvaluationState.FAILED, EvaluationState.SCORING, threshold, LocalDateTime.now());
    }
}
