package com.javis.learn_hub.category.service.dto;

import com.javis.learn_hub.category.domain.CategoryNode;
import java.util.ArrayList;
import java.util.List;

public record AllCategoryNodesResponse(
        List<CategoryNodeResponse> categoryNodeResponses
) {

    public static AllCategoryNodesResponse from(List<CategoryNode> categoryNodes) {
        List<CategoryNodeResponse> categoryNodeResponses = new ArrayList<>();
        for (CategoryNode categoryNode : categoryNodes) {
            categoryNodeResponses.add(CategoryNodeResponse.from(categoryNode));
        }
        return new AllCategoryNodesResponse(categoryNodeResponses);
    }
}
