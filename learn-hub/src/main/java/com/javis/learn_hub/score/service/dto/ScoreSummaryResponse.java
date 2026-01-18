package com.javis.learn_hub.score.service.dto;

import java.util.List;

public record ScoreSummaryResponse(
        List<MainCategoryScoreResponse> scores
) {}

