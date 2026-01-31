package com.javis.learn_hub.answer.service.dto;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.evaluation.domain.EvaluationStatus;

public record AnswerStatusResponse(
        Long answerId,
        EvaluationStatus status,
        String grade,
        String feedback,
        Integer score
) {
    public static AnswerStatusResponse pending(Answer answer) {
        return new AnswerStatusResponse(
                answer.getId(),
                EvaluationStatus.PENDING,
                null,
                null,
                null
        );
    }

    public static AnswerStatusResponse from(Answer answer, Evaluation evaluation) {
        return new AnswerStatusResponse(
                answer.getId(),
                evaluation.getStatus(),
                evaluation.getResult().getGrade().name(),
                evaluation.getFeedback(),
                evaluation.getScore()
        );
    }
}
