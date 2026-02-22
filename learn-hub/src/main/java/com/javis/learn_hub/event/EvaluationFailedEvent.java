package com.javis.learn_hub.event;

public record EvaluationFailedEvent(
        Long questionId,
        Long memberId
) {

}
