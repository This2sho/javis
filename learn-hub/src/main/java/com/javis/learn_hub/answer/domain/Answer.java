package com.javis.learn_hub.answer.domain;

import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.domain.CreatedOnlyEntity;
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
public class Answer extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AssociationConverter.class)
    private Association<Question> questionId;

    @Lob
    private String message;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private EvaluationStatus evaluationStatus;

    public Answer(Association<Question> questionId, String message) {
        this.questionId = questionId;
        this.message = message;
        this.evaluationStatus = EvaluationStatus.PENDING;
    }

    public static Answer create(Long questionId, String message) {
        return new Answer(Association.from(questionId), message);
    }

    public void toScoring() {
        this.evaluationStatus = this.evaluationStatus.toScoring();
    }

    public void success() {
        this.evaluationStatus = this.evaluationStatus.success();
    }

    public void fail() {
        this.evaluationStatus = this.evaluationStatus.fail();
    }

    public boolean needsEvaluation() {
        return this.evaluationStatus == EvaluationStatus.PENDING
            || this.evaluationStatus == EvaluationStatus.FAILED;
    }

    public boolean isPendingEvaluation() {
        return this.evaluationStatus != EvaluationStatus.SCORED;
    }
}
