package com.javis.learn_hub.event;

public record InterviewFinishEvent(Long interviewId, Long memberId) implements DomainEvent {

}
