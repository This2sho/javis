package com.javis.learn_hub.event;

import com.javis.learn_hub.problem.domain.Difficulty;
import java.util.List;

public record EvaluationCompletedEvent(
        Long answerId,
        Long questionId,
        Long memberId,
        List<Difficulty> preferences
) implements DomainEvent {
}
