package com.javis.learn_hub.category.service.dto;

import com.javis.learn_hub.category.domain.CategoryNode;
import java.util.List;

public record CategoryNodeResponse(
        String name,
        List<CategoryNodeResponse> children
) {

    public static CategoryNodeResponse from(CategoryNode node) {
        return new CategoryNodeResponse(
                node.getCategoryName(),
                node.getChildren().values().stream()
                        .map(CategoryNodeResponse::from)
                        .toList()
        );
    }
}
