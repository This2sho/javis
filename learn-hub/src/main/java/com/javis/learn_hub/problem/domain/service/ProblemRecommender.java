package com.javis.learn_hub.problem.domain.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.repository.ProblemRepository;
import com.javis.learn_hub.score.domain.service.CategoryRecommender;
import com.javis.learn_hub.support.domain.Association;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
            int recommendSize
    ) {
        List<Association<Category>> categories = categoryRecommender.recommendCategoryIdsByScore(mainCategory,
                memberId, recommendSize);
        List<Problem> rootProblems = problemRepository.findRecommendableRootProblems(categories, Association.getEmpty(), Association.from(memberId));
        return sortByRecommended(rootProblems, recommendSize);
    }

    private List<Problem> sortByRecommended(List<Problem> problems, int recommendSize) {
        if (problems.size() > recommendSize) {
            problems = new ArrayList<>(problems.subList(0, recommendSize));
        }
        Collections.shuffle(problems);
        return problems;
    }

    public Optional<Problem> recommendNextProblem(Association<Problem> previousProblemId,
                                                  List<Association<Problem>> excludeProblemIds,
                                                  List<Difficulty> preferences) {
        List<Problem> followUpProblems = problemReader.getFollowUpProblems(previousProblemId);
        List<Problem> unSubmittedFollowUpProblems = excludeSubmittedProblems(followUpProblems, excludeProblemIds);
        if (canNotRecommendCondition(preferences, unSubmittedFollowUpProblems)) {
            return Optional.empty();
        }
        return findProblemByPreferences(preferences, unSubmittedFollowUpProblems);
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
