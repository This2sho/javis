package com.javis.learn_hub.interview.service.dto;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.dto.QnA;
import com.javis.learn_hub.interview.domain.Question;

public record QnAResponse(String question, String answer, String feedBack) {

    public static QnAResponse from(QnA qnA) {
        Question question = qnA.question();
        Answer answer = qnA.answer();
        return new QnAResponse(question.getMessage(), answer.getMessage(), answer.getFeedback());
    }
}
