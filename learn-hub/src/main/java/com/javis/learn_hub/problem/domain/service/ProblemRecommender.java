package com.javis.learn_hub.problem.domain.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.repository.ProblemRepository;
import com.javis.learn_hub.score.domain.service.CategoryRecommender;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProblemRecommender {

    private final CategoryRecommender categoryRecommender;
    private final ProblemReader problemReader;
    private final ProblemRepository problemRepository;

    public List<Problem> recommendRootProblems(
            Long memberId,
            MainCategory mainCategory,
            ContentLanguage contentLanguage,
            int recommendSize
    ) {
        List<Association<Category>> preferredCategories = categoryRecommender.recommendCategoryIdsByScore(
                mainCategory,
                memberId,
                recommendSize
        );
        List<Association<Category>> orderedCategories = mergeCategoryPriority(
                preferredCategories,
                categoryRecommender.getAllCategoryIds(mainCategory)
        );
        if (orderedCategories.isEmpty()) {
            return Collections.emptyList();
        }
        List<Problem> rootProblems = problemRepository.findRecommendableRootProblems(
                orderedCategories,
                Association.getEmpty(),
                Association.from(memberId),
                contentLanguage,
                contentLanguage.isKorean()
        );
        return sortByRecommended(orderedCategories, rootProblems, recommendSize);
    }

    public List<Problem> recommendRootProblems(Long memberId, MainCategory mainCategory, int recommendSize) {
        return recommendRootProblems(memberId, mainCategory, ContentLanguage.KO, recommendSize);
    }

    private List<Association<Category>> mergeCategoryPriority(List<Association<Category>> preferredCategories,
                                                              List<Association<Category>> allCategories) {
        LinkedHashSet<Association<Category>> categoryOrder = new LinkedHashSet<>(preferredCategories);
        categoryOrder.addAll(allCategories);
        return new ArrayList<>(categoryOrder);
    }

    private List<Problem> sortByRecommended(List<Association<Category>> orderedCategories,
                                            List<Problem> problems,
                                            int recommendSize) {
        Map<Association<Category>, List<Problem>> problemsByCategory = problems.stream()
                .collect(Collectors.groupingBy(Problem::getCategoryId, Collectors.toCollection(ArrayList::new)));
        problemsByCategory.values().forEach(Collections::shuffle);

        List<Problem> selectedProblems = new ArrayList<>();
        while (selectedProblems.size() < recommendSize) {
            boolean pickedInRound = false;
            for (Association<Category> categoryId : orderedCategories) {
                List<Problem> categoryProblems = problemsByCategory.get(categoryId);
                if (categoryProblems == null || categoryProblems.isEmpty()) {
                    continue;
                }
                selectedProblems.add(categoryProblems.remove(0));
                pickedInRound = true;
                if (selectedProblems.size() == recommendSize) {
                    return selectedProblems;
                }
            }
            if (!pickedInRound) {
                break;
            }
        }
        return selectedProblems;
    }

    public Optional<Problem> recommendNextProblem(Association<Problem> previousProblemId,
                                                  List<Association<Problem>> excludeProblemIds,
                                                  List<Difficulty> preferences,
                                                  ContentLanguage contentLanguage) {
        List<Problem> followUpProblems = problemReader.getFollowUpProblems(previousProblemId);
        List<Problem> unSubmittedFollowUpProblems = excludeSubmittedProblems(followUpProblems, excludeProblemIds)
                .stream()
                .filter(problem -> matchesContentLanguage(problem, contentLanguage))
                .collect(Collectors.toList());
        if (canNotRecommendCondition(preferences, unSubmittedFollowUpProblems)) {
            return Optional.empty();
        }
        return findProblemByPreferences(preferences, unSubmittedFollowUpProblems);
    }

    public Optional<Problem> recommendNextProblem(Association<Problem> previousProblemId,
                                                  List<Association<Problem>> excludeProblemIds,
                                                  List<Difficulty> preferences) {
        return recommendNextProblem(previousProblemId, excludeProblemIds, preferences, null);
    }

    private boolean matchesContentLanguage(Problem problem, ContentLanguage contentLanguage) {
        if (contentLanguage == null) {
            return true;
        }
        if (problem.getContentLanguage() == null) {
            return contentLanguage.isKorean();
        }
        return problem.isIn(contentLanguage);
    }

    private List<Problem> excludeSubmittedProblems(List<Problem> followUpProblems,
                                                   List<Association<Problem>> excludeProblemIds) {
        return followUpProblems.stream()
                .filter(problem -> !excludeProblemIds.contains(Association.from(problem.getId())))
                .collect(Collectors.toList());
    }

    private boolean canNotRecommendCondition(List<Difficulty> preferences, List<Problem> followUpProblems) {
        return preferences.isEmpty() || followUpProblems.isEmpty();
    }

    private Optional<Problem> findProblemByPreferences(List<Difficulty> preferences,
                                                       List<Problem> unSubmittedFollowUpProblems) {
        return unSubmittedFollowUpProblems.stream()
                .sorted(Comparator.comparingInt(problem -> preferences.indexOf(problem.getDifficulty())))
                .findFirst();
    }
}
