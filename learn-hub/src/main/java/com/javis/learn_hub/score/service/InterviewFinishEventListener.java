package com.javis.learn_hub.score.service;

import com.javis.learn_hub.event.InterviewFinishEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InterviewFinishEventListener {

    private final ScoreService scoreService;

    @EventListener
    public void listen(InterviewFinishEvent event) {
        scoreService.applyScore(event.interviewId(), event.memberId());
    }
}
