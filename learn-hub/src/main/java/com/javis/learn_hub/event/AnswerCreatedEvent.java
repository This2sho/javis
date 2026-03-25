package com.javis.learn_hub.event;

public record AnswerCreatedEvent(
        Long answerId,
        Long questionId
) implements DomainEvent {
}
