package com.javis.learn_hub.event;

public record EvaluationRetryEvent(
        Long questionId
) implements DomainEvent {

}
