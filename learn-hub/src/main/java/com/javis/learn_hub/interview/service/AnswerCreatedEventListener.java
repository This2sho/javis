package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.event.AnswerCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class AnswerCreatedEventListener {

    private final InterviewCommandService interviewCommandService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAnswerCreated(AnswerCreatedEvent event) {
        log.info("답변 생성 이벤트 수신: questionId={}", event.questionId());
        interviewCommandService.markQuestionAnswered(event.questionId());
    }
}
