package com.javis.learn_hub.answer.domain.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.EvaluationState;
import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class AnswerProcessor {

    private final AnswerRepository answerRepository;
    private final AnswerReader answerReader;

    public AnswerCreatedEvent create(Long questionId, String userAnswer) {
        Answer answer = Answer.create(questionId, userAnswer);
        answerRepository.save(answer);
        return new AnswerCreatedEvent(answer.getId(), questionId, userAnswer);
    }

    @Transactional
    public boolean prepareScoring(Long answerId) {
        Answer answer = answerReader.get(answerId);
        answer.validateCanStartScoring();
        return answerRepository.startScoring(answerId);
    }

    @Transactional
    public boolean completeScoring(Answer answer) {
        answer.validateCanChangeScored();
        return answerRepository.completeScoring(answer.getId());
    }

    @Transactional
    public void failScoring(Long answerId) {
        Answer answer = answerReader.get(answerId);
        answer.validateCanChangeFailed();
        answerRepository.failScoring(answerId);
    }

    @Transactional
    public void recoverScoringAnswers() {
        List<Answer> scoringAnswers = answerRepository.findAllByEvaluationState(EvaluationState.SCORING);
        scoringAnswers.forEach(answer -> {
            answer.validateCanChangeFailed();
            answerRepository.failScoring(answer.getId());
        });
    }
}
