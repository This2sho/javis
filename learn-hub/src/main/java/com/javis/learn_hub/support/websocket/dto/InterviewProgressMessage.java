package com.javis.learn_hub.support.websocket.dto;

public record InterviewProgressMessage(
        boolean ended,
        boolean failed,
        Long interviewId,
        Long questionId,
        String interviewerMessage
) {

}
