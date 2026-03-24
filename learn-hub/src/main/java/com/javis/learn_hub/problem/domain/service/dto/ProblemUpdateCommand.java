package com.javis.learn_hub.problem.domain.service.dto;

import com.javis.learn_hub.problem.domain.Difficulty;
import java.util.List;

public record ProblemUpdateCommand(
        Long problemId,
        String problem,
        String referenceAnswer,
        Difficulty difficulty,
        String categoryPath,
        List<ProblemUpdateCommand> followUpProblems,
        List<Long> deletedProblemIds
) {

    public ProblemCreateCommand toCreateCommand() {
        return new ProblemCreateCommand(problem, referenceAnswer, difficulty, categoryPath,
                followUpProblems.stream().map(ProblemUpdateCommand::toCreateCommand).toList());
    }

    public boolean isNewProblem() {
        return problemId == null;
    }

    public boolean hasNoFollowUps() {
        return followUpProblems == null || followUpProblems.isEmpty();
    }

    public boolean hasDeletedProblems() {
        return deletedProblemIds != null && !deletedProblemIds.isEmpty();
    }
}
