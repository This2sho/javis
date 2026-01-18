package com.javis.learn_hub.problem.domain.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.dto.ProblemDetailView;
import com.javis.learn_hub.problem.domain.service.dto.ProblemDetailWithCategoryView;
import com.javis.learn_hub.problem.domain.service.dto.ProblemTreeView;
import com.javis.learn_hub.problem.domain.service.dto.ProblemUpdateCommand;
import com.javis.learn_hub.support.domain.Association;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProblemFinder {

    private final ProblemReader problemReader;
    private final CategoryReader categoryReader;

    public Map<Long, Category> getAllCategory(List<Problem> problems) {
        Set<Long> categoryIds = getCategoryIds(problems);
        Map<Long, Category> categoriesById = categoryReader.getAll(categoryIds)
                .stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        Function.identity()
                ));

        return problems.stream()
                .collect(Collectors.toMap(Problem::getId,
                        problem -> categoriesById.get(problem.getCategoryId().getId())));
    }

    private Set<Long> getCategoryIds(List<Problem> problems) {
        return problems.stream()
                .map(problem -> problem.getCategoryId().getId())
                .collect(Collectors.toSet());
    }

    public ProblemTreeView findProblemTree(Long problemId) {
        ProblemDetailWithCategoryView rootDetail = findProblemDetailWithCategory(problemId);
        return buildTree(rootDetail);
    }

    private ProblemDetailWithCategoryView findProblemDetailWithCategory(Long problemId) {
        ProblemDetailView problemDetail = findProblemDetail(problemId);
        Problem problem = problemDetail.problem();
        ProblemScoringInfo problemScoringInfo = problemDetail.problemScoringInfo();
        Category category = categoryReader.get(problem.getCategoryId().getId());
        return new ProblemDetailWithCategoryView(problem, problemScoringInfo, category);
    }

    private ProblemTreeView buildTree(ProblemDetailWithCategoryView current) {
        Long currentProblemId = current.problem().getId();
        List<Problem> followUpProblems = problemReader.getFollowUpProblems(Association.from(currentProblemId));
        List<ProblemTreeView> childTrees = followUpProblems.stream()
                .map(problem -> {
                    ProblemDetailWithCategoryView childDetail = findProblemDetailWithCategory(problem.getId());
                    return buildTree(childDetail);
                }).toList();
        return new ProblemTreeView(current, childTrees);
    }

    public ProblemDetailView findProblemDetail(Long problemId) {
        Problem problem = problemReader.get(problemId);
        ProblemScoringInfo problemScoringInfo = problemReader.getProblemScoringInfo(problemId);
        return new ProblemDetailView(problem, problemScoringInfo);
    }

    public Map<Long, ProblemDetailWithCategoryView> findAll(ProblemUpdateCommand command) {
        Map<Long, ProblemDetailWithCategoryView> result = new HashMap<>();
        findAllRecursive(command, result);
        return result;
    }

    private void findAllRecursive(ProblemUpdateCommand command, Map<Long, ProblemDetailWithCategoryView> result) {
        if (command.isNewProblem()) {
            return;
        }
        Long problemId = command.problemId();
        ProblemDetailView problemDetail = findProblemDetail(problemId);
        Category category = categoryReader.get(command.categoryPath());
        ProblemDetailWithCategoryView problemDetailWithCategoryView = new ProblemDetailWithCategoryView(
                problemDetail.problem(), problemDetail.problemScoringInfo(), category);
        result.put(problemId, problemDetailWithCategoryView);

        if (command.hasNoFollowUps()) {
            return;
        }
        for (ProblemUpdateCommand followUp : command.followUpProblems()) {
            findAllRecursive(followUp, result);
        }
    }
}
