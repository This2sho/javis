package com.javis.learn_hub.interview.service.dto;

import com.javis.learn_hub.interview.domain.Question;

public record QuestionResponse(Long questionId, boolean isFirst,String question) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getId(), true, question.getMessage());
    }

    public static QuestionResponse continueFrom(Question question) {
        return new QuestionResponse(question.getId(), false, question.getMessage());
    }
}
