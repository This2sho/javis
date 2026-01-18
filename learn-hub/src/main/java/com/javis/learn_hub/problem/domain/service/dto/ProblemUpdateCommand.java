package com.javis.learn_hub.problem.domain.service.dto;

import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Keywords;
import java.util.List;

public record ProblemUpdateCommand(
        Long problemId,
        String problem,
        String referenceAnswer,
        Keywords keywords,
        Difficulty difficulty,
        String categoryPath,
        List<ProblemUpdateCommand> followUpProblems
) {

    public ProblemCreateCommand toCreateCommand() {
        return new ProblemCreateCommand(problem, referenceAnswer, keywords, difficulty, categoryPath,
                followUpProblems.stream().map(ProblemUpdateCommand::toCreateCommand).toList());
    }

    public boolean isNewProblem() {
        return problemId == null;
    }

    public boolean hasNoFollowUps() {
        return followUpProblems == null || followUpProblems.isEmpty();
    }
}
