package com.javis.learn_hub.interview.service.dto;

public record InterviewerResponse(boolean ended, boolean failed, Long interviewId, Long questionId, String interviewerMessage) {

    public static InterviewerResponse nextQuestion(Long interviewId, Long questionId, String interviewerMessage) {
        return new  InterviewerResponse(false, false, interviewId, questionId, interviewerMessage);
    }

    public static InterviewerResponse fail() {
        return new InterviewerResponse(false, true, null, null, "채점 중 오류가 발생했습니다.");
    }


    public static InterviewerResponse finished(Long interviewId) {
        return new InterviewerResponse(true, false, interviewId,-1L, "고생하셨습니다. 인터뷰가 종료되었습니다.");
    }
}
