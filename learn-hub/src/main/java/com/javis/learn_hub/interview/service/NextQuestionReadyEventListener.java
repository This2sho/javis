package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.event.NextQuestionReadyEvent;
import com.javis.learn_hub.support.websocket.InterviewWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class NextQuestionReadyEventListener {

    private final InterviewWebSocketHandler webSocketHandler;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNextQuestionReady(NextQuestionReadyEvent event) {
        log.info("다음 질문 전송: memberId={}", event.memberId());
        webSocketHandler.sendNextQuestion(event.memberId(), event.response());
    }
}
