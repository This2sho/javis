package com.javis.learn_hub.score.service.dto;

public record PracticeFocusResponse(
        String categoryPath,
        String mainCategoryPath,
        String reason,
        int totalScore,
        long attemptCount,
        double recentAverageScore,
        Long averageResponseTimeMs,
        String latestFeedback
) {
}
