package com.javis.learn_hub.evaluation.domain.analysis;

public record SentenceAnnotation(
        String sentenceId,
        String issueType,
        String reason,
        String suggestion
) {
}
