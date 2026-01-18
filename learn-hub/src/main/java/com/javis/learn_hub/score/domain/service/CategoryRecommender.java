package com.javis.learn_hub.score.domain.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.support.domain.Association;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CategoryRecommender {

    private final ScoreReader scoreReader;
    private final CategoryReader categoryReader;

    public List<Association<Category>> recommendCategoryIdsByScore(MainCategory mainCategory, Long memberId, int maxRecommendSize) {
        List<Category> subCategories = categoryReader.getAllSubCategoriesFrom(mainCategory);
        List<Score> scoresByLowest = scoreReader.getScoresByLowest(Association.from(memberId), subCategories);
        List<Association<Category>> existingCategoryIds = scoresByLowest.stream().map(score -> score.getCategoryId()).toList();
        List<Association<Category>> zeroScoreCategoryIds = getZeroScoreCategories(existingCategoryIds, subCategories);
        List<Association<Category>> result = new ArrayList<>(zeroScoreCategoryIds);
        if (zeroScoreCategoryIds.size() > maxRecommendSize) {
            return new ArrayList<>(result.subList(0, maxRecommendSize));
        }
        int neededSize = maxRecommendSize - zeroScoreCategoryIds.size();
        result.addAll(existingCategoryIds.subList(0, Math.min(neededSize, existingCategoryIds.size())));
        return result;
    }

    private List<Association<Category>> getZeroScoreCategories(List<Association<Category>> categoryIds,
                                                        List<Category> subCategories) {
        Set<Association<Category>> existingCategories = new HashSet<>(categoryIds);
        return subCategories.stream()
                .map(category -> Association.<Category>from(category.getId()))
                .filter(category -> !existingCategories.contains(Association.from(category.getId())))
                .toList();
    }
}
