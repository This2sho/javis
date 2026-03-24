package com.javis.learn_hub.answer.domain.repository;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationState;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.Repository;

public interface AnswerRepository extends Repository<Answer, Long> {

    Answer save(Answer answer);

    Optional<Answer> findById(Long id);

    List<Answer> findAllByQuestionIdIn(List<Association<Question>> questionIds);

    Optional<Answer> findByQuestionId(Association<Question> questionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Answer a
        SET a.evaluationState = :failedState,
            a.updatedAt = :now
        WHERE a.evaluationState = :scoringState
          AND a.updatedAt < :cutoff
    """)
    int failStaleScoringAnswers(@Param("scoringState") EvaluationState scoringState,
                                @Param("failedState") EvaluationState failedState,
                                @Param("cutoff") LocalDateTime cutoff,
                                @Param("now") LocalDateTime now);
}
