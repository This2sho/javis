package com.javis.learn_hub.problem.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public record ProblemUpdateRequest(
        @NotNull
        Long id,

        @NotBlank
        String problem,

        @NotBlank
        String referenceAnswer,

        @NotEmpty
        Set<String> keywords,

        @NotBlank
        String difficulty,

        @NotBlank
        String category,

        List<ProblemUpdateRequest> followUpProblems
) {

}
