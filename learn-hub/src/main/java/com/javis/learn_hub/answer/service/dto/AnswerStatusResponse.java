package com.javis.learn_hub.answer.service.dto;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationStatus;
import com.javis.learn_hub.evaluation.domain.Evaluation;

public record AnswerStatusResponse(
        Long answerId,
        EvaluationStatus evaluationState,
        String grade,
        String feedback,
        Integer score
) {
    public static AnswerStatusResponse pending(Answer answer) {
        return new AnswerStatusResponse(
                answer.getId(),
                answer.getEvaluationState(),
                null,
                null,
                null
        );
    }

    public static AnswerStatusResponse from(Answer answer, Evaluation evaluation) {
        return new AnswerStatusResponse(
                answer.getId(),
                answer.getEvaluationState(),
                evaluation.getResult().getGrade().name(),
                evaluation.getFeedback(),
                evaluation.getScore()
        );
    }
}
