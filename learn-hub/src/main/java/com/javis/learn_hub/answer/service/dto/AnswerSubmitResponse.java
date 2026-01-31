package com.javis.learn_hub.answer.service.dto;

public record AnswerSubmitResponse(
        Long answerId,
        String status,
        String message
) {
    public static AnswerSubmitResponse accepted(Long answerId) {
        return new AnswerSubmitResponse(answerId, "PENDING", "답변이 제출되었습니다. 채점 중입니다.");
    }
}
