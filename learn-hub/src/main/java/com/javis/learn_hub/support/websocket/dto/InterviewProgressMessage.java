package com.javis.learn_hub.support.websocket.dto;

public record InterviewProgressMessage (
        boolean ended,
        Long interviewId,
        Long questionId,
        String interviewerMessage
){

}
