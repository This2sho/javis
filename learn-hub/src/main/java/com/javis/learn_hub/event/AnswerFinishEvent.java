package com.javis.learn_hub.event;

public record AnswerFinishEvent(Long questionId) implements DomainEvent {

}
