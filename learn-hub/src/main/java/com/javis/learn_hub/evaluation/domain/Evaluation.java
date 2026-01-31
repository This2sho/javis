package com.javis.learn_hub.evaluation.domain;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.domain.CreatedOnlyEntity;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "evaluation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_evaluation_answer_id",
                        columnNames = {"answer_id"}
                )
        }
)
public class Evaluation extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AssociationConverter.class)
    private Association<Answer> answerId;

    @Enumerated(EnumType.STRING)
    private EvaluationStatus status;

    @Embedded
    private EvaluationResult result;

    public Evaluation(Association<Answer> answerId, EvaluationStatus status, EvaluationResult result) {
        this.answerId = answerId;
        this.status = status;
        this.result = result;
    }

    public static Evaluation completed(Long answerId, EvaluationResult result) {
        return new Evaluation(Association.from(answerId), EvaluationStatus.COMPLETED, result);
    }

    public int getScore() {
        return result.getScore();
    }

    public String getFeedback() {
        return result.getFeedback();
    }
}
