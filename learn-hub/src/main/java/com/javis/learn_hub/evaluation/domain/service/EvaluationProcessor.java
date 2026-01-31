package com.javis.learn_hub.evaluation.domain.service;

import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.evaluation.domain.EvaluationResult;
import com.javis.learn_hub.evaluation.domain.Grade;
import com.javis.learn_hub.evaluation.domain.repository.EvaluationRepository;
import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.event.EvaluationCompletedEvent;
import java.util.ArrayList;
import java.util.List;
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
    public List<DomainEvent> complete(Long answerId, Long questionId, Long memberId, String grade, String feedback) {
        EvaluationResult result = new EvaluationResult(toGrade(grade), feedback);
        Evaluation evaluation = Evaluation.completed(answerId, result);
        evaluationRepository.save(evaluation);

        List<DomainEvent> events = new ArrayList<>();
        events.add(new EvaluationCompletedEvent(answerId, questionId, memberId, result.getPreferences()));
        return events;
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
