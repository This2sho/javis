package com.javis.learn_hub.score.domain.service;

import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.score.domain.repository.ScoreRepository;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ScoreReader {

    private final ScoreRepository scoreRepository;
    private final CategoryReader categoryReader;

    public Set<Score> getAllBy(Association<Member> memberId, List<CategoryGrade> categoryGrades) {
        return scoreRepository.findByMemberIdAndCategoryIdIn(memberId, toCategoryIds(categoryGrades));
    }

    public List<Score> getScoresByLowest(Association<Member> memberId, List<Category> categories) {
        List<Association<Category>> categoryIds = categoryIdsFrom(categories);
        return scoreRepository.findByMemberIdAndCategoryIdIn(memberId, categoryIds)
                .stream().sorted().toList();
    }

    private List<Association<Category>> toCategoryIds(List<CategoryGrade> categoryGrades) {
        return categoryGrades.stream()
                .map(categoryGrade -> Association.<Category>from(categoryGrade.categoryId()))
                .toList();
    }

    private List<Association<Category>> categoryIdsFrom(List<Category> categories) {
        return categories.stream()
                .map(Category::getId)
                .map(id -> Association.<Category>from(id))
                .toList();
    }

    public int getMainCategoryScore(Association<Member> memberId, MainCategory mainCategory) {
        List<Category> subCategories = categoryReader.getAllSubCategoriesFrom(mainCategory);
        return scoreRepository.sumScoresByMemberIdAndCategoryIdIn(memberId, categoryIdsFrom(subCategories));
    }

    public Map<Category, Integer> getAllSubCategoryScores(Association<Member> memberId, MainCategory mainCategory) {
        List<Category> subCategories = categoryReader.getAllSubCategoriesFrom(mainCategory);
        Set<Score> scores = scoreRepository.findByMemberIdAndCategoryIdIn(memberId, categoryIdsFrom(subCategories));
        Map<Long, Score> scoreByCategoryId = collectScoresByCategoryId(scores);
        return collectScoresByCategoryPath(subCategories, scoreByCategoryId);
    }

    private Map<Long, Score> collectScoresByCategoryId(Set<Score> scores) {
        return scores.stream()
                .collect(Collectors.toMap(
                        score -> score.getCategoryId().getId(),
                        Function.identity()
                ));
    }

    private Map<Category, Integer> collectScoresByCategoryPath(List<Category> subCategories, Map<Long, Score> scoreByCategoryId) {

        return subCategories.stream()
                .filter(category -> scoreByCategoryId.containsKey(category.getId()))
                .collect(Collectors.toMap(
                        Function.identity(),
                        category -> scoreByCategoryId.get(category.getId()).getScore()
                ));
    }
}
