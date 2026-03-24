package com.javis.learn_hub.evaluation.domain;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.domain.CreatedOnlyEntity;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import com.javis.learn_hub.problem.domain.Difficulty;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import java.util.List;
import jakarta.persistence.Entity;
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

    @Embedded
    private EvaluationResult result;

    public Evaluation(Association<Answer> answerId, EvaluationResult result) {
        this.answerId = answerId;
        this.result = result;
    }

    public static Evaluation completed(Long answerId, EvaluationResult result) {
        return new Evaluation(Association.from(answerId), result);
    }

    public int getScore() {
        return result.getScore();
    }

    public String getFeedback() {
        return result.getFeedback();
    }

    public List<Difficulty> getPreferences() {
        return result.getPreferences();
    }
}
