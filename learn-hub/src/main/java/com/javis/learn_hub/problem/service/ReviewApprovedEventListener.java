package com.javis.learn_hub.problem.service;

import com.javis.learn_hub.event.ReviewApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReviewApprovedEventListener {

    private final ProblemCommandService problemCommandService;

    @EventListener
    public void listen(ReviewApprovedEvent event) {
        problemCommandService.publish(event.rootProblemId());
    }
}
