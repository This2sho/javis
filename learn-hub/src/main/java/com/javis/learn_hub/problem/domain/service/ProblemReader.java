package com.javis.learn_hub.problem.domain.service;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.repository.ProblemRepository;
import com.javis.learn_hub.problem.domain.repository.ProblemScoringInfoRepository;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    public List<Problem> getAllRootProblem(Long memberId, CursorPageRequest pageRequest) {
        Association<Member> member = Association.from(memberId);
        if (pageRequest.isDesc()) {
            return problemRepository.findAllByMemberIdAndParentProblemIdByLatest(pageRequest.getTargetTime(), pageRequest.getTargetId(),
                    member, Association.getEmpty(), pageRequest.getPageable());
        }
        return problemRepository.findAllByMemberIdAndParentProblemIdByOldest(pageRequest.getTargetTime(), pageRequest.getTargetId(),
                member, Association.getEmpty(), pageRequest.getPageable());
    }

    public Problem get(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 문제입니다."));
    }

    public ProblemScoringInfo getProblemScoringInfo(Long problemId) {
        return problemScoringInfoRepository.findByProblemId(Association.from(problemId))
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 문제입니다."));
    }
}
