package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationState;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import org.springframework.test.util.ReflectionTestUtils;

public class AnswerBuilder {

    private Association<Question> questionId = Association.from(1L);
    private String message = "기본 답변입니다.";

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

    public Answer build() {
        return new Answer(questionId, message);
    }

    public Answer buildScored() {
        Answer answer = build();
        ReflectionTestUtils.setField(answer, "evaluationState", EvaluationState.SCORED);
        return answer;
    }
}
