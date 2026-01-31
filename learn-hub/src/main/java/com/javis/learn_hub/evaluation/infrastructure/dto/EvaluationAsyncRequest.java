package com.javis.learn_hub.evaluation.infrastructure.dto;

import java.util.Set;

public record EvaluationAsyncRequest(
        Long answerId,
        String referenceAnswer,
        Set<String> keywords,
        String userAnswer,
        String callbackUrl
) {
}
