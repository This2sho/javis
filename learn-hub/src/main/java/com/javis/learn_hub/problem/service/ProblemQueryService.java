package com.javis.learn_hub.problem.service;

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
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProblemQueryService {

    private final ProblemReader problemReader;
    private final ProblemFinder problemFinder;

    public CursorPageResponse<ProblemHistoryResponse> viewHistories(Long memberId, CursorPageRequest cursorPageRequest) {
        List<Problem> problems = problemReader.getAllRootProblem(memberId, cursorPageRequest);
        CursorPage<Problem> slicedProblems = CursorPagingSupport.slice(problems, cursorPageRequest);
        Map<Long, Category> categoriesByProblemId = problemFinder.getAllCategory(slicedProblems.content());
        return collectToResponse(slicedProblems, categoriesByProblemId);
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
