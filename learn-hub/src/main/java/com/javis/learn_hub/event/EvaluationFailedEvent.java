package com.javis.learn_hub.event;

import com.javis.learn_hub.interview.service.dto.InterviewerResponse;

public record EvaluationFailedEvent(
        Long answerId,
        Long questionId,
        InterviewerResponse response
) {
    public static EvaluationFailedEvent of(Long answerId, Long questionId) {
        return new EvaluationFailedEvent(answerId, questionId, InterviewerResponse.fail());
    }
}
