package com.javis.learn_hub.problem.domain;

import com.javis.learn_hub.problem.domain.infrastructure.KeywordsConverter;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.infrastructure.AssociationConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"id"})
@Entity
public class ProblemScoringInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AssociationConverter.class)
    private Association<Problem> problemId;

    @Lob
    private String referenceAnswer;

    @Convert(converter = KeywordsConverter.class)
    private Keywords keywords;

    public ProblemScoringInfo(Association<Problem> problemId, String referenceAnswer, Keywords keywords) {
        this.problemId = problemId;
        this.referenceAnswer = referenceAnswer;
        this.keywords = keywords;
    }

    public void update(String referenceAnswer, Keywords keywords) {
        if (!isUpdated(referenceAnswer, keywords)) {
            return;
        }
        this.referenceAnswer = referenceAnswer;
        this.keywords = keywords;
    }

    private boolean isUpdated(String referenceAnswer, Keywords keywords) {
        if (this.referenceAnswer.equals(referenceAnswer) && this.keywords.equals(keywords)) {
            return false;
        }
        return true;
    }
}
