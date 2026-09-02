package com.javis.learn_hub.problem.domain.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.repository.ProblemRepository;
import com.javis.learn_hub.problem.domain.repository.ProblemScoringInfoRepository;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProblemReader {

    private final ProblemRepository problemRepository;
    private final ProblemScoringInfoRepository problemScoringInfoRepository;

    public List<Problem> getFollowUpProblems(Association<Problem> problemId) {
        return problemRepository.findAllByParentProblemId(problemId);
    }

    public List<Problem> getAll(Iterable<Long> problemIds) {
        return problemRepository.findAllByIdIn(problemIds);
    }

    public List<Problem> getRecommendableRootProblems(List<Association<Category>> categoryIds, Long memberId) {
        return problemRepository.findRecommendableRootProblems(
                categoryIds,
                Association.getEmpty(),
                Association.from(memberId),
                ContentLanguage.KO,
                true
        );
    }

    public List<Problem> getAllRootProblem(Long memberId, CursorPageRequest pageRequest, ContentLanguage contentLanguage) {
        return getAllRootProblem(memberId, pageRequest, null, null, contentLanguage);
    }

    public List<Problem> getAllRootProblem(Long memberId, CursorPageRequest pageRequest) {
        return getAllRootProblem(memberId, pageRequest, ContentLanguage.KO);
    }

    public List<Problem> getAllRootProblem(Long memberId, CursorPageRequest pageRequest, String mainCategoryPath,
                                           String rootCategoryPath, ContentLanguage contentLanguage) {
        boolean includeNullLanguage = contentLanguage.isKorean();
        Pageable nativeQueryPageable = PageRequest.of(0, pageRequest.getPageable().getPageSize());
        if (pageRequest.isDesc()) {
            return problemRepository.findAllRootByMemberAndFiltersByLatest(
                    pageRequest.getTargetTime(),
                    pageRequest.getTargetId(),
                    memberId,
                    mainCategoryPath,
                    rootCategoryPath,
                    contentLanguage.name(),
                    includeNullLanguage,
                    nativeQueryPageable
            );
        }
        return problemRepository.findAllRootByMemberAndFiltersByOldest(
                pageRequest.getTargetTime(),
                pageRequest.getTargetId(),
                memberId,
                mainCategoryPath,
                rootCategoryPath,
                contentLanguage.name(),
                includeNullLanguage,
                nativeQueryPageable
        );
    }

    public Problem get(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 문제입니다."));
    }

    public ProblemScoringInfo getProblemScoringInfo(Long problemId) {
        return problemScoringInfoRepository.findByProblemId(Association.from(problemId))
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 문제입니다."));
    }

    public List<ProblemScoringInfo> getAllProblemScoringInfos(List<Association<Problem>> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return List.of();
        }
        return problemScoringInfoRepository.findAllByProblemIdIn(problemIds);
    }

    public ProblemScoringInfo getProblemScoringInfoByQuestionId(Long questionId) {
        return problemScoringInfoRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 문제입니다."));
    }
}
