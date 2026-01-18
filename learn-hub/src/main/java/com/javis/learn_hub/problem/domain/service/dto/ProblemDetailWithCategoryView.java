package com.javis.learn_hub.problem.domain.service.dto;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;

public record ProblemDetailWithCategoryView(
        Problem problem,
        ProblemScoringInfo problemScoringInfo,
        Category category
) {

}
