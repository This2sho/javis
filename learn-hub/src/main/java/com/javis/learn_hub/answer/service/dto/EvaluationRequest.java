package com.javis.learn_hub.answer.service.dto;

import java.util.Set;

public record EvaluationRequest(
        String problem,
        String referenceAnswer,
        Set<String> keywords,
        String userAnswer
) {

}
