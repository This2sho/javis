package com.javis.learn_hub.answer.domain.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationState;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AnswerProcessor {

    private final AnswerRepository answerRepository;
    private final AnswerReader answerReader;

    public AnswerCreatedEvent create(Long questionId, String userAnswer) {
        Answer answer = Answer.create(questionId, userAnswer);
        answerRepository.save(answer);
        return new AnswerCreatedEvent(answer.getId(), questionId);
    }

    public Answer prepareScoring(Long questionId) {
        Answer answer = answerReader.getByQuestionId(questionId);
        answer.toScoring();
        answerRepository.save(answer);
        return answer;
    }

    public void success(Long answerId) {
        Answer answer = answerReader.get(answerId);
        answer.success();
        answerRepository.save(answer);
    }

    public void fail(Long answerId) {
        Answer answer = answerReader.get(answerId);
        answer.fail();
        answerRepository.save(answer);
    }

    public int recoverScoringAnswers(LocalDateTime staleThreshold, LocalDateTime now) {
        return answerRepository.failStaleScoringAnswers(
                EvaluationState.SCORING,
                EvaluationState.FAILED,
                staleThreshold,
                now
        );
    }
}
