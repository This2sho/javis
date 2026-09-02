package com.javis.learn_hub.problem.service;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemFinder;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.problem.domain.service.dto.ProblemTreeView;
import com.javis.learn_hub.problem.service.dto.ProblemHistoryDetailResponse;
import com.javis.learn_hub.problem.service.dto.ProblemHistoryResponse;
import com.javis.learn_hub.support.application.CursorPagingSupport;
import com.javis.learn_hub.support.application.dto.CursorPage;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorPageResponse;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProblemQueryService {

    private final ProblemReader problemReader;
    private final ProblemFinder problemFinder;

    public CursorPageResponse<ProblemHistoryResponse> viewHistories(Long memberId,
                                                                    CursorPageRequest cursorPageRequest,
                                                                    String mainCategory,
                                                                    String rootCategoryPath,
                                                                    ContentLanguage contentLanguage) {
        MainCategory resolved = resolveMainCategory(mainCategory);
        String mainCategoryPath = resolved != null ? resolved.getPath() : null;
        ContentLanguage resolvedLanguage = resolved != null
                ? resolved.resolveContentLanguage(contentLanguage)
                : contentLanguage;
        String normalizedRootCategoryPath = normalizeRootCategory(rootCategoryPath);
        List<Problem> problems = problemReader.getAllRootProblem(
                memberId,
                cursorPageRequest,
                mainCategoryPath,
                normalizedRootCategoryPath,
                resolvedLanguage
        );
        CursorPage<Problem> slicedProblems = CursorPagingSupport.slice(problems, cursorPageRequest);
        Map<Long, Category> categoriesByProblemId = problemFinder.getAllCategory(slicedProblems.content());
        return collectToResponse(slicedProblems, categoriesByProblemId);
    }

    private MainCategory resolveMainCategory(String mainCategory) {
        if (mainCategory == null || mainCategory.isBlank()) {
            return null;
        }
        return MainCategory.from(mainCategory);
    }

    private String normalizeRootCategory(String rootCategoryPath) {
        if (rootCategoryPath == null || rootCategoryPath.isBlank()) {
            return null;
        }
        return rootCategoryPath.toLowerCase();
    }

    private CursorPageResponse<ProblemHistoryResponse> collectToResponse(CursorPage<Problem> slicedProblems, Map<Long, Category> categoriesByProblemId) {
        List<ProblemHistoryResponse> responses = slicedProblems.content()
                .stream()
                .map(problem -> ProblemHistoryResponse.of(problem, categoriesByProblemId.get(problem.getId())))
                .toList();
        return new CursorPageResponse(responses, slicedProblems.nextCursor(), slicedProblems.hasNext());
    }

    public ProblemHistoryDetailResponse viewHistory(Long problemId) {
        ProblemTreeView problemTreeView = problemFinder.findProblemTree(problemId);
        return ProblemHistoryDetailResponse.from(problemTreeView);
    }
}
