package com.javis.learn_hub.evaluation.domain.service;

import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.evaluation.domain.EvaluationResult;
import com.javis.learn_hub.evaluation.domain.Grade;
import com.javis.learn_hub.evaluation.domain.repository.EvaluationRepository;
import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import com.javis.learn_hub.event.EvaluationCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EvaluationProcessor {

    private final EvaluationRepository evaluationRepository;

    /**
     * 채점 완료 처리 (InterviewProcessor.finish 패턴)
     * Evaluation 생성 후 발행할 이벤트 목록 반환
     */
    public EvaluationCompletedEvent complete(Long answerId, Long questionId, EvaluationResponse response) {
        EvaluationResult result = new EvaluationResult(toGrade(response.grade()), response.feedback());
        Evaluation evaluation = Evaluation.completed(answerId, result);
        evaluationRepository.save(evaluation);
        return new EvaluationCompletedEvent(answerId, questionId, result.getPreferences());
    }

    private Grade toGrade(String grade) {
        return switch (grade.toLowerCase()) {
            case "perfect" -> Grade.PERFECT;
            case "good" -> Grade.GOOD;
            case "vague" -> Grade.VAGUE;
            case "incorrect" -> Grade.INCORRECT;
            default -> throw new IllegalArgumentException("Unknown grade: " + grade);
        };
    }
}
