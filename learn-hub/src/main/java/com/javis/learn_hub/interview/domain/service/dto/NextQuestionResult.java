package com.javis.learn_hub.interview.domain.service.dto;

import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import java.util.Optional;

public record NextQuestionResult(
        Interview interview,
        Optional<Question> nextQuestion
) {

    public static NextQuestionResult withNextQuestion(Interview interview, Question question) {
        return new NextQuestionResult(interview, Optional.of(question));
    }

    public static NextQuestionResult finished(Interview interview) {
        return new NextQuestionResult(interview, Optional.empty());
    }

    public boolean hasNextQuestion() {
        return nextQuestion.isPresent();
    }
}
