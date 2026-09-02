package com.javis.learn_hub.evaluation.domain.analysis;

public record SegmentedSentence(
        String sentenceId,
        String text,
        int startIndex,
        int endIndex
) {
}
