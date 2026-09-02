package com.javis.learn_hub.score.service.dto;

import java.util.List;

public record ScoreDetailResponse(
        CategoryScoreNodeResponse scoreTree,
        List<PracticeFocusResponse> practiceFocuses,
        EnglishCoachingResponse englishCoaching
) {
}
