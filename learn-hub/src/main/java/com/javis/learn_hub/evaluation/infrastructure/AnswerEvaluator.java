package com.javis.learn_hub.evaluation.infrastructure;

import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;

public interface AnswerEvaluator {
    EvaluationResponse evaluate(String question, String referenceAnswer, String userAnswer);
}
