package com.javis.learn_hub.event;

public record AnswerCreatedEvent(
        Long answerId,
        Long questionId,
        String userAnswer
) implements DomainEvent {
}
