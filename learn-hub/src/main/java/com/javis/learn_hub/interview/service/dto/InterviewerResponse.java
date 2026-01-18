package com.javis.learn_hub.interview.service.dto;

public record InterviewerResponse(boolean ended, Long interviewId, Long questionId, String interviewerMessage) {

    public static InterviewerResponse finished(Long interviewId) {
        return new InterviewerResponse(true, interviewId,-1L, "고생하셨습니다. 인터뷰가 종료되었습니다.");
    }
}
