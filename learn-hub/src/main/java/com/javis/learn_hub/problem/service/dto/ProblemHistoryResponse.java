package com.javis.learn_hub.problem.service.dto;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.problem.domain.Problem;

public record ProblemHistoryResponse(
        Long id,
        String content,
        String difficulty,
        String category
) {

    public static ProblemHistoryResponse of(Problem problem, Category category) {
        return new ProblemHistoryResponse(
                problem.getId(),
                problem.getContent(),
                problem.getDifficulty().name(),
                category.getPath()
        );
    }
}
