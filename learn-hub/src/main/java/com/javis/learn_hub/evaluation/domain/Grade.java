package com.javis.learn_hub.evaluation.domain;

import static com.javis.learn_hub.problem.domain.Difficulty.EASY;
import static com.javis.learn_hub.problem.domain.Difficulty.HARD;
import static com.javis.learn_hub.problem.domain.Difficulty.MEDIUM;

import com.javis.learn_hub.problem.domain.Difficulty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public enum Grade {
    PERFECT(4),
    GOOD(3),
    VAGUE(1),
    INCORRECT(0);

    private final int score;

    Grade(int score) {
        this.score = score;
    }

    public List<Difficulty> getPreferences() {
        return switch (this) {
            case PERFECT -> List.of(HARD, MEDIUM, EASY);
            case GOOD -> List.of(MEDIUM, HARD, EASY);
            case VAGUE -> List.of(EASY, MEDIUM, HARD);
            default -> Collections.emptyList();
        };
    }
}
