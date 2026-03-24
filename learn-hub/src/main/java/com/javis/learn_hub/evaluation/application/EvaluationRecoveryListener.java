package com.javis.learn_hub.evaluation.application;

import com.javis.learn_hub.answer.service.AnswerCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationRecoveryListener {

    private final AnswerCommandService answerCommandService;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverScoringAnswers() {
        answerCommandService.recoverScoringAnswers();
    }
}
