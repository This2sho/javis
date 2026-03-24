package com.javis.learn_hub.problem.domain.service.dto;

import com.javis.learn_hub.problem.domain.Difficulty;
import java.util.List;

public record ProblemCreateCommand(
        String problem,
        String referenceAnswer,
        Difficulty difficulty,
        String categoryPath,
        List<ProblemCreateCommand> followUps
) {

}
