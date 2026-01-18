package com.javis.learn_hub.score.service.dto;

import com.javis.learn_hub.category.domain.MainCategory;

public record MainCategoryScoreResponse(
        MainCategory mainCategory,
        int score
) {}

