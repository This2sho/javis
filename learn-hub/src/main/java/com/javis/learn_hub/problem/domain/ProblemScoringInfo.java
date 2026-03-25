package com.javis.learn_hub.problem.domain;

import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"id"})
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_scoring_info_problem_id",
                        columnNames = "problem_id"
                )
        }
)
public class ProblemScoringInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AssociationConverter.class)
    private Association<Problem> problemId;

    @Lob
    private String referenceAnswer;

    public ProblemScoringInfo(Association<Problem> problemId, String referenceAnswer) {
        this.problemId = problemId;
        this.referenceAnswer = referenceAnswer;
    }

    public void update(String referenceAnswer) {
        if (this.referenceAnswer.equals(referenceAnswer)) {
            return;
        }
        this.referenceAnswer = referenceAnswer;
    }
}
