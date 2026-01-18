package com.javis.learn_hub.problem.domain.service.dto;

import java.util.List;

public record ProblemTreeView(
        ProblemDetailWithCategoryView problemDetailWithCategoryView,
        List<ProblemTreeView> children
) {

}
