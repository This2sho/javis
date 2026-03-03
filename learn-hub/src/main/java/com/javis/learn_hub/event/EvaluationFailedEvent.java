package com.javis.learn_hub.event;

public record EvaluationFailedEvent(
        Long answerId,
        Long questionId,
        Long memberId
) {

}
