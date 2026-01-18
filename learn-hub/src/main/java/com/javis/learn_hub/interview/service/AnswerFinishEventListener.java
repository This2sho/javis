package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.event.AnswerFinishEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AnswerFinishEventListener {

    private final InterviewCommandService interviewCommandService;

    @EventListener
    public void listen(AnswerFinishEvent event) {
        interviewCommandService.completeCurrentQuestion(event.questionId());
    }
}
