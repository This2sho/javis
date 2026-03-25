package com.javis.learn_hub.answer.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.answer.service.dto.AnswerRequest;
import com.javis.learn_hub.answer.service.dto.AnswerSubmitResponse;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnswerCommandService {

    private final AnswerProcessor answerProcessor;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public AnswerSubmitResponse submitAnswer(Long questionId, AnswerRequest request) {
        AnswerCreatedEvent event = answerProcessor.create(questionId, request.userAnswer());
        applicationEventPublisher.publishEvent(event);
        return AnswerSubmitResponse.accepted(event.answerId());
    }

    @Transactional
    public Optional<Answer> prepareScoring(Long questionId) {
        try {
            Answer answer = answerProcessor.prepareScoring(questionId);
            return Optional.of(answer);
        } catch (IllegalStateException | ObjectOptimisticLockingFailureException e) {
            log.info("이미 채점 진행 중, 중복 요청 무시: questionId={}", questionId);
            return Optional.empty();
        }
    }

    @Transactional
    public void failEvaluation(Long answerId) {
        answerProcessor.fail(answerId);
    }

    @Transactional
    public void successEvaluation(Long answerId) {
        answerProcessor.success(answerId);
    }

    @Transactional
    public int recoverScoringAnswers() {
        LocalDateTime now = LocalDateTime.now();
        return answerProcessor.recoverScoringAnswers(now.minusMinutes(5), now);
    }
}
