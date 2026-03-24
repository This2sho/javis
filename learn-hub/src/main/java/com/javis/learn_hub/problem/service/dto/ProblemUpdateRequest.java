package com.javis.learn_hub.problem.service.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ProblemUpdateRequest(
        Long problemId,

        @NotBlank
        String problem,

        @NotBlank
        String referenceAnswer,

        @NotBlank
        String difficulty,

        @NotBlank
        String category,

        List<ProblemUpdateRequest> followUpProblems,

        List<Long> deletedProblemIds
) {

}
