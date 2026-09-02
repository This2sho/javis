package com.javis.learn_hub.evaluation.domain.analysis;

import java.util.List;

public record EvaluationAnalysis(
        List<SegmentedSentence> sentences,
        List<SentenceAnnotation> sentenceAnnotations,
        List<MissingPoint> missingPoints
) {
    public static EvaluationAnalysis empty(List<SegmentedSentence> sentences) {
        return new EvaluationAnalysis(sentences, List.of(), List.of());
    }
}
