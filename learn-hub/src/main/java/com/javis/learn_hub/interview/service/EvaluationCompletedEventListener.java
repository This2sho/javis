package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.event.EvaluationCompletedEvent;
import com.javis.learn_hub.event.NextQuestionReadyEvent;
import com.javis.learn_hub.interview.service.dto.InterviewerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationCompletedEventListener {

    private final InterviewCommandService interviewCommandService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onEvaluationCompleted(EvaluationCompletedEvent event) {
        log.info("채점 완료 이벤트 수신: answerId={}, questionId={}", event.answerId(), event.questionId());

        InterviewerResponse response = interviewCommandService.continueNextQuestion(
                event.questionId(),
                event.preferences()
        );

        eventPublisher.publishEvent(new NextQuestionReadyEvent(event.memberId(), response));
    }
}
