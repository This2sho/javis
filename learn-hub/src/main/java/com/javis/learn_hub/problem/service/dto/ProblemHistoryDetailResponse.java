package com.javis.learn_hub.problem.service.dto;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.dto.ProblemDetailWithCategoryView;
import com.javis.learn_hub.problem.domain.service.dto.ProblemTreeView;
import java.util.List;
import java.util.Set;

public record ProblemHistoryDetailResponse(
        Long id,
        String content,
        String difficulty,
        String category,
        String referenceAnswer,
        Set<String> keywords,
        List<ProblemHistoryDetailResponse> followUps
) {

    public static ProblemHistoryDetailResponse from(ProblemTreeView node) {
        ProblemDetailWithCategoryView detail = node.problemDetailWithCategoryView();
        Problem problem = detail.problem();
        Category category = detail.category();
        ProblemScoringInfo scoringInfo = detail.problemScoringInfo();

        List<ProblemHistoryDetailResponse> followUps =
                node.children().stream()
                        .map(ProblemHistoryDetailResponse::from)
                        .toList();

        return new ProblemHistoryDetailResponse(
                problem.getId(),
                problem.getContent(),
                problem.getDifficulty().name(),
                category.getPath(),
                scoringInfo.getReferenceAnswer(),
                scoringInfo.getKeywords().getKeywords(),
                followUps
        );

    }
}
