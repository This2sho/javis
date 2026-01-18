package com.javis.learn_hub.event;

public record ReviewApprovedEvent(Long rootProblemId) implements DomainEvent {

}
