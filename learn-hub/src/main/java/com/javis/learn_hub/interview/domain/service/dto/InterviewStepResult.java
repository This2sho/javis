package com.javis.learn_hub.interview.domain.service.dto;

import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.service.dto.QuestionResponse;
import com.javis.learn_hub.problem.domain.Difficulty;
import java.util.List;

public record InterviewStepResult(
        QuestionResponse questionResponse,
        Long questionId,
        List<Difficulty> preferences
) {

    public static InterviewStepResult continueCurrentQuestion(Question question) {
        return new InterviewStepResult(
                QuestionResponse.continueFrom(question),
                question.getId(),
                List.of()
        );
    }

    public static InterviewStepResult pendingEvaluation(Question question) {
        return new InterviewStepResult(
                QuestionResponse.pendingEvaluation(question),
                question.getId(),
                List.of()
        );
    }

    public static InterviewStepResult waitingForNextQuestion(Question question, List<Difficulty> preferences) {
        return new InterviewStepResult(
                QuestionResponse.waitingForNextQuestion(question),
                question.getId(),
                preferences
        );
    }

    public boolean needsRetryEvaluation() {
        return questionResponse.isPendingEvaluation();
    }

    public boolean needsNextQuestion() {
        return !preferences.isEmpty();
    }
}
