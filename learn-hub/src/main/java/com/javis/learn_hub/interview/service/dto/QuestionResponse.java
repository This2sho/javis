package com.javis.learn_hub.interview.service.dto;

import com.javis.learn_hub.interview.domain.Question;

public record QuestionResponse(
        Long questionId,
        Long interviewId,
        boolean isFirst,
        boolean isPendingEvaluation,
        String question
) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getInterviewId().getId(),
                true,
                false,
                question.getMessage()
        );
    }

    public static QuestionResponse continueFrom(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getInterviewId().getId(),
                false,
                false,
                question.getMessage()
        );
    }

    public static QuestionResponse pendingEvaluation(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getInterviewId().getId(),
                false,
                true,
                null
        );
    }
}
