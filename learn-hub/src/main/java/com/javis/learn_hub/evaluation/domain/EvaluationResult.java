package com.javis.learn_hub.evaluation.domain;

import com.javis.learn_hub.problem.domain.Difficulty;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class EvaluationResult {

    @Enumerated(EnumType.STRING)
    private Grade grade;
    private String feedback;

    public EvaluationResult(Grade grade, String feedback) {
        this.grade = grade;
        this.feedback = feedback;
    }

    public int getScore() {
        return grade.getScore();
    }

    public List<Difficulty> getPreferences() {
        return grade.getPreferences();
    }
}
