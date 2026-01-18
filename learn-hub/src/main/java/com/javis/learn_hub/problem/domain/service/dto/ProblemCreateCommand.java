package com.javis.learn_hub.problem.domain.service.dto;

import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Keywords;
import java.util.List;

public record ProblemCreateCommand(
        String problem,
        String referenceAnswer,
        Keywords keywords,
        Difficulty difficulty,
        String categoryPath,
        List<ProblemCreateCommand> followUps
) {

}
