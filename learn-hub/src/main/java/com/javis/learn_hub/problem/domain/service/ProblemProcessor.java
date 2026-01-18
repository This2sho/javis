package com.javis.learn_hub.problem.domain.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.service.CategoryProcessor;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.Visibility;
import com.javis.learn_hub.problem.domain.repository.ProblemRepository;
import com.javis.learn_hub.problem.domain.repository.ProblemScoringInfoRepository;
import com.javis.learn_hub.problem.domain.service.dto.ProblemCreateCommand;
import com.javis.learn_hub.problem.domain.service.dto.ProblemDetailWithCategoryView;
import com.javis.learn_hub.problem.domain.service.dto.ProblemUpdateCommand;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProblemProcessor {

    private final ProblemRepository problemRepository;
    private final ProblemScoringInfoRepository problemScoringInfoRepository;
    private final ProblemFinder problemFinder;
    private final CategoryProcessor categoryProcessor;

    public Problem create(ProblemCreateCommand command, Long writerId, Visibility visibility) {
        Problem rootProblem = create(command, Association.from(writerId), Association.getEmpty(),
                visibility);
        createFollowUpProblems(command.followUps(), rootProblem);
        return rootProblem;
    }

    private Problem create(ProblemCreateCommand command, Association<Member> writerId,
                           Association<Problem> parentProblemId, Visibility visibility) {
        Category category = categoryProcessor.makeIfAbsentByPath(command.categoryPath());
        Problem problem = new Problem(
                Association.from(category.getId()),
                parentProblemId,
                writerId,
                command.difficulty(),
                command.problem(),
                visibility
        );
        problemRepository.save(problem);
        ProblemScoringInfo problemScoringInfo = new ProblemScoringInfo(Association.from(problem.getId()),
                command.referenceAnswer(), command.keywords());
        problemScoringInfoRepository.save(problemScoringInfo);
        return problem;
    }

    private void createFollowUpProblems(List<ProblemCreateCommand> commands, Problem parentProblem) {
        if (commands == null || commands.size() == 0) {
            return;
        }

        Association<Problem> parentId = Association.from(parentProblem.getId());

        for (ProblemCreateCommand command : commands) {
            Problem problem = create(command, parentProblem.getWriterId(), parentId, Visibility.INHERITED);
            createFollowUpProblems(command.followUps(), problem);
        }
    }

    public void update(ProblemUpdateCommand command, Association<Member> writerId) {
        Map<Long, ProblemDetailWithCategoryView> map = problemFinder.findAll(command);
        if (map.isEmpty()) {
            throw new IllegalArgumentException("문제가 존재하지 않습니다.");
        }
        applyUpdateRecursively(command, map, Association.getEmpty(), writerId);
    }

    private void applyUpdateRecursively(
            ProblemUpdateCommand command,
            Map<Long, ProblemDetailWithCategoryView> map,
            Association<Problem> parentProblemId,
            Association<Member> writerId
    ) {
        if (command.isNewProblem()) {
            createNewProblemWithFollowUps(command, parentProblemId, writerId);
            return;
        }
        Problem problem = updateExistingProblem(command, map);
        updateFollowUps(command, map, writerId, problem);
    }

    private void createNewProblemWithFollowUps(ProblemUpdateCommand command, Association<Problem> parentProblemId,
                           Association<Member> writerId) {
        ProblemCreateCommand createCommand = command.toCreateCommand();
        Problem created = create(createCommand, writerId, parentProblemId, Visibility.INHERITED);
        createFollowUpProblems(createCommand.followUps(), created);
    }

    private Problem updateExistingProblem(ProblemUpdateCommand command, Map<Long, ProblemDetailWithCategoryView> map) {
        Long problemId = command.problemId();
        ProblemDetailWithCategoryView view = map.get(problemId);
        Problem problem = view.problem();
        ProblemScoringInfo scoringInfo = view.problemScoringInfo();
        Category category = view.category();

        problem.update(Association.from(category.getId()), command.difficulty(), command.problem());
        scoringInfo.update(command.referenceAnswer(), command.keywords());
        return problem;
    }

    private void updateFollowUps(ProblemUpdateCommand command, Map<Long, ProblemDetailWithCategoryView> map,
                                 Association<Member> writerId, Problem problem) {
        if (command.hasNoFollowUps()) {
            return;
        }
        for (ProblemUpdateCommand followUp : command.followUpProblems()) {
            applyUpdateRecursively(followUp, map, Association.from(problem.getId()), writerId);
        }
    }
}
