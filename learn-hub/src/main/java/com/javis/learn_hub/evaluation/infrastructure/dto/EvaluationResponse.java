package com.javis.learn_hub.evaluation.infrastructure.dto;

import com.javis.learn_hub.evaluation.domain.analysis.MissingPoint;
import com.javis.learn_hub.evaluation.domain.analysis.SentenceAnnotation;
import java.util.List;

public record EvaluationResponse(
        String evaluationLogic,
        String grade,
        String feedback,
        List<SentenceAnnotation> sentenceAnnotations,
        List<MissingPoint> missingPoints
) {
}
