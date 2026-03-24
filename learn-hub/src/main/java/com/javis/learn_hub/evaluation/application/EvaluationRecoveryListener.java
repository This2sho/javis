package com.javis.learn_hub.evaluation.application;

import com.javis.learn_hub.answer.service.AnswerCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationRecoveryListener {

    private final AnswerCommandService answerCommandService;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverScoringAnswers() {
        int recovered = answerCommandService.recoverScoringAnswers();
        if (recovered > 0) {
            log.warn("애플리케이션 시작 시 5분 이상 지난 SCORING 상태 답변 {}건 복구", recovered);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void recoverStuckScoringAnswers() {
        int recovered = answerCommandService.recoverScoringAnswers();
        if (recovered > 0) {
            log.warn("주기적으로 5분 이상 지난 SCORING 상태 답변 {}건 복구", recovered);
        }
    }
}
