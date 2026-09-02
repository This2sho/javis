package com.javis.learn_hub.answer.domain;

import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.domain.BaseEntity;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "answer",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_answer_question_id",
                        columnNames = {"question_id"}
                )
        }
)
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AssociationConverter.class)
    private Association<Question> questionId;

    @Lob
    private String message;

    private Long responseTimeMs;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private EvaluationState evaluationState;

    public Answer(Association<Question> questionId, String message, Long responseTimeMs) {
        this.questionId = questionId;
        this.message = message;
        this.responseTimeMs = sanitizeResponseTime(responseTimeMs);
        this.evaluationState = EvaluationState.PENDING;
    }

    public Answer(Association<Question> questionId, String message) {
        this(questionId, message, null);
    }

    public static Answer create(Long questionId, String message, Long responseTimeMs) {
        return new Answer(Association.from(questionId), message, responseTimeMs);
    }

    public static Answer create(Long questionId, String message) {
        return create(questionId, message, null);
    }

    private Long sanitizeResponseTime(Long responseTimeMs) {
        if (responseTimeMs == null) {
            return null;
        }
        return Math.max(0L, responseTimeMs);
    }

    public void toScoring() {
        this.evaluationState = this.evaluationState.toScoring();
    }

    public void success() {
        this.evaluationState = this.evaluationState.success();
    }

    public void fail() {
        this.evaluationState = this.evaluationState.fail();
    }

    public void requeue() {
        this.evaluationState = this.evaluationState.requeue();
    }

    public boolean needsEvaluation() {
        return this.evaluationState == EvaluationState.PENDING
            || this.evaluationState == EvaluationState.FAILED;
    }

    public boolean isPendingEvaluation() {
        return this.evaluationState != EvaluationState.SCORED;
    }
}
