package com.javis.learn_hub.evaluation.domain.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.evaluation.domain.repository.EvaluationRepository;
import com.javis.learn_hub.support.domain.Association;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EvaluationReader {

    private final EvaluationRepository evaluationRepository;

    public Evaluation getByAnswerId(Long answerId) {
        return evaluationRepository.findByAnswerId(Association.from(answerId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채점 결과입니다."));
    }
}
