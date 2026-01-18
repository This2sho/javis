package com.javis.learn_hub.answer.domain.service;

import com.javis.learn_hub.answer.domain.EvaluationResult;
import com.javis.learn_hub.answer.service.dto.EvaluationRequest;

@FunctionalInterface
public interface Evaluator {

    EvaluationResult evaluate(EvaluationRequest request);
}
