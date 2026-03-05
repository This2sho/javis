package com.javis.learn_hub.event;

import com.javis.learn_hub.interview.service.dto.InterviewerResponse;

public record NextQuestionReadyEvent(
        Long memberId,
        InterviewerResponse response
) {
}
