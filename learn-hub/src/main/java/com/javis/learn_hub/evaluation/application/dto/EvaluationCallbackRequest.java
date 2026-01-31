package com.javis.learn_hub.evaluation.application.dto;

public record EvaluationCallbackRequest(
        Long answerId,
        String grade,
        String feedback
) {
}