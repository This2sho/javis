package com.javis.learn_hub.problem.service;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.Visibility;
import com.javis.learn_hub.problem.domain.service.ProblemProcessor;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.problem.domain.service.dto.ProblemCreateCommand;
import com.javis.learn_hub.problem.domain.service.dto.ProblemUpdateCommand;
import com.javis.learn_hub.problem.service.dto.ProblemCreateRequest;
import com.javis.learn_hub.problem.service.dto.ProblemUpdateRequest;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProblemCommandService {

    private final ProblemProcessor problemProcessor;
    private final ProblemReader problemReader;

    @Transactional
    public Long create(ProblemCreateRequest request, Long memberId) {
        ProblemCreateCommand command = toProblemCreateCommand(request);
        Problem problem = problemProcessor.create(command, memberId, Visibility.PRIVATE);
        return problem.getId();
    }

    private ProblemCreateCommand toProblemCreateCommand(ProblemCreateRequest request) {
        return new ProblemCreateCommand(request.problem(), request.referenceAnswer(),
                Difficulty.from(request.difficulty()), request.category(),
                request.followUpProblems() == null ? List.of()
                        : request.followUpProblems().stream().map(this::toProblemCreateCommand).toList());
    }

    @Transactional
    public void update(ProblemUpdateRequest request, Long memberId) {
        Problem problem = problemReader.get(request.problemId());
        Association<Member> writerId = Association.from(memberId);
        problem.validateWriter(writerId);
        ProblemUpdateCommand command = toProblemUpdateCommand(request);
        problemProcessor.update(command, writerId);
    }

    @Transactional
    public void delete(Long problemId, Long memberId) {
        Problem problem = problemReader.get(problemId);
        Association<Member> writerId = Association.from(memberId);
        problem.validateWriter(writerId);
        problemProcessor.delete(problemId);
    }

    private ProblemUpdateCommand toProblemUpdateCommand(ProblemUpdateRequest request) {
        return new ProblemUpdateCommand(request.problemId(), request.problem(), request.referenceAnswer(),
                Difficulty.from(request.difficulty()), request.category(),
                request.followUpProblems() == null ? List.of()
                        : request.followUpProblems().stream().map(this::toProblemUpdateCommand).toList(),
                request.deletedProblemIds());
    }

    /**
     * admin용 API
     */
    @Transactional
    public void createAll(List<ProblemCreateRequest> requests, Long memberId) {
        for (ProblemCreateRequest request : requests) {
            ProblemCreateCommand command = toProblemCreateCommand(request);
            problemProcessor.create(command, memberId, Visibility.PUBLIC);
        }
    }

    public void publish(Long rootProblemId) {
        Problem problem = problemReader.get(rootProblemId);
        problem.publish();
    }
}
