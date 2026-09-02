package com.javis.learn_hub.score.service.dto;

public record CommonMistakeResponse(
        String sentence,
        String suggestion,
        String reason,
        long count
) {
}
