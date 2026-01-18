package com.javis.learn_hub.score.service.dto;

import com.javis.learn_hub.category.domain.CategoryScoreNode;
import java.util.List;

public record CategoryScoreNodeResponse(
        String categoryName,
        int score,
        List<CategoryScoreNodeResponse> children
) {

    public static CategoryScoreNodeResponse from(CategoryScoreNode node) {
        return new CategoryScoreNodeResponse(
                node.getCategoryName(),
                node.getTotalScore(),
                node.getChildren().values().stream()
                        .map(CategoryScoreNodeResponse::from)
                        .toList()
        );
    }
}
