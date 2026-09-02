package com.javis.learn_hub.score.service.dto;

import java.util.List;

public record EnglishCoachingResponse(
        List<MemorizeSentenceResponse> memorizeSentences,
        List<CommonMistakeResponse> commonMistakes
) {
    public boolean isEmpty() {
        return memorizeSentences.isEmpty() && commonMistakes.isEmpty();
    }
}
