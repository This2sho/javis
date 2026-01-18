package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationResult;
import com.javis.learn_hub.answer.domain.Grade;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;

public class AnswerBuilder {

    private Association<Question> questionId = Association.from(1L);
    private String message = "기본 답변입니다.";
    private EvaluationResult evaluationResult =
            new EvaluationResult(Grade.GOOD, "기본 평가입니다.");

    public static AnswerBuilder builder() {
        return new AnswerBuilder();
    }

    public AnswerBuilder withQuestionId(Long questionId) {
        this.questionId = Association.from(questionId);
        return this;
    }

    public AnswerBuilder withQuestionId(Association<Question> questionId) {
        this.questionId = questionId;
        return this;
    }

    public AnswerBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public AnswerBuilder withEvaluationResult(EvaluationResult evaluationResult) {
        this.evaluationResult = evaluationResult;
        return this;
    }

    public Answer build() {
        return new Answer(questionId, message, evaluationResult);
    }
}
