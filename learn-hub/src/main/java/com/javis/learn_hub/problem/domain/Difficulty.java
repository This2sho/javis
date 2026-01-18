package com.javis.learn_hub.problem.domain;

public enum Difficulty {
    EASY, MEDIUM, HARD;

    public static Difficulty from(String difficulty) {
        return Difficulty.valueOf(difficulty.toUpperCase());
    }
}
