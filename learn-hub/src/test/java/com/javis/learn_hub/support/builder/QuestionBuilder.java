package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.support.domain.Association;

public class QuestionBuilder {

    private Association<Problem> problemId = Association.from(1L);
    private Association<Interview> interviewId = Association.from(1L);
    private Association<Question> parentQuestionId = Association.getEmpty();
    private QuestionStatus questionStatus = QuestionStatus.UNANSWERED;
    private int questionOrder = 0;
    private String message = "기본 질문입니다.";

    public static QuestionBuilder builder() {
        return new QuestionBuilder();
    }

    // ---- with 메서드들 ----
    public QuestionBuilder withProblemId(Long problemId) {
        this.problemId = Association.from(problemId);
        return this;
    }

    public QuestionBuilder withProblemId(Association<Problem> problemId) {
        this.problemId = problemId;
        return this;
    }

    public QuestionBuilder withInterviewId(Long interviewId) {
        this.interviewId = Association.from(interviewId);
        return this;
    }

    public QuestionBuilder withInterviewId(Association<Interview> interviewId) {
        this.interviewId = interviewId;
        return this;
    }

    public QuestionBuilder withQuestionStatus(QuestionStatus questionStatus) {
        this.questionStatus = questionStatus;
        return this;
    }

    public QuestionBuilder withParentQuestionId(Long parentQuestionId) {
        this.parentQuestionId = Association.from(parentQuestionId);
        return this;
    }

    public QuestionBuilder withParentQuestionId(Association<Question> parentQuestionId) {
        this.parentQuestionId = parentQuestionId;
        return this;
    }

    public QuestionBuilder withQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
        return this;
    }

    public QuestionBuilder withMessage(String message) {
        this.message = message;
        return this;
    }


    // ---- root question 생성 ----
    public Question buildRoot() {
        Question question = Question.rootQuestionOf(
                problemId,
                interviewId,
                questionOrder,
                message
        );
        if (questionStatus == QuestionStatus.ANSWERED) {
            question.complete();
        }
        return question;
    }

    // ---- follow-up question 생성 ----
    public Question buildFollowUp() {
        if (parentQuestionId.isEmpty()) {
            throw new IllegalStateException("follow-up question을 만들려면 parentQuestionId가 필요합니다.");
        }

        Question question = new Question(
                problemId,
                interviewId,
                parentQuestionId,
                0,               // follow-up question은 order 0
                message
        );
        if (questionStatus == QuestionStatus.ANSWERED) {
            question.complete();
        }
        return question;
    }

    // 일반적인 build (root/follow-up 구분 없이)
    public Question build() {
        Question question = new Question(
                problemId,
                interviewId,
                parentQuestionId,
                questionOrder,
                message
        );
        if (questionStatus == QuestionStatus.ANSWERED) {
            question.complete();
        }
        return question;
    }
}
