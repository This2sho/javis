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
import com.javis.learn_hub.review.domain.repository.ReviewRepository;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.i18n.ContentLanguage;
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
    private final ReviewRepository reviewRepository;

    public Problem create(ProblemCreateCommand command, Long writerId, Visibility visibility,
                          ContentLanguage contentLanguage) {
        Problem rootProblem = create(command, Association.from(writerId), Association.getEmpty(),
                visibility, contentLanguage);
        createFollowUpProblems(command.followUps(), rootProblem, contentLanguage);
        return rootProblem;
    }

    public Problem create(ProblemCreateCommand command, Long writerId, Visibility visibility) {
        return create(command, writerId, visibility, ContentLanguage.KO);
    }

    private Problem create(ProblemCreateCommand command, Association<Member> writerId,
                           Association<Problem> parentProblemId, Visibility visibility,
                           ContentLanguage contentLanguage) {
        Category category = categoryProcessor.makeIfAbsentByPath(command.categoryPath());
        Problem problem = new Problem(
                Association.from(category.getId()),
                parentProblemId,
                writerId,
                command.difficulty(),
                command.problem(),
                visibility,
                contentLanguage
        );
        problemRepository.save(problem);
        ProblemScoringInfo problemScoringInfo = new ProblemScoringInfo(Association.from(problem.getId()),
                command.referenceAnswer());
        problemScoringInfoRepository.save(problemScoringInfo);
        return problem;
    }

    private void createFollowUpProblems(List<ProblemCreateCommand> commands, Problem parentProblem,
                                        ContentLanguage contentLanguage) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        Association<Problem> parentId = Association.from(parentProblem.getId());

        for (ProblemCreateCommand command : commands) {
            Problem problem = create(command, parentProblem.getWriterId(), parentId, Visibility.INHERITED, contentLanguage);
            createFollowUpProblems(command.followUps(), problem, contentLanguage);
        }
    }

    public void update(ProblemUpdateCommand command, Association<Member> writerId) {
        Map<Long, ProblemDetailWithCategoryView> map = problemFinder.findAll(command);
        if (map.isEmpty()) {
            throw new IllegalArgumentException("문제가 존재하지 않습니다.");
        }

        if (command.hasDeletedProblems()) {
            deleteProblems(command.deletedProblemIds());
        }

        applyUpdateRecursively(command, map, Association.getEmpty(), writerId, null);
    }

    public void delete(Long rootProblemId) {
        deleteRecursively(Association.from(rootProblemId));
        reviewRepository.deleteByRootProblemId(Association.from(rootProblemId));
    }

    private void deleteProblems(List<Long> problemIds) {
        for (Long problemId : problemIds) {
            problemScoringInfoRepository.deleteByProblemId(Association.from(problemId));
            problemRepository.deleteById(problemId);
        }
    }

    private void deleteRecursively(Association<Problem> problemId) {
        List<Problem> followUpProblems = problemRepository.findAllByParentProblemId(problemId);
        for (Problem followUpProblem : followUpProblems) {
            deleteRecursively(Association.from(followUpProblem.getId()));
        }

        problemScoringInfoRepository.deleteByProblemId(problemId);
        problemRepository.deleteById(problemId.getId());
    }

    private void applyUpdateRecursively(
            ProblemUpdateCommand command,
            Map<Long, ProblemDetailWithCategoryView> map,
            Association<Problem> parentProblemId,
            Association<Member> writerId,
            ContentLanguage contentLanguage
    ) {
        if (command.isNewProblem()) {
            createNewProblemWithFollowUps(command, parentProblemId, writerId, contentLanguage);
            return;
        }
        Problem problem = updateExistingProblem(command, map);
        updateFollowUps(command, map, writerId, problem,
                problem.getContentLanguage() == null ? ContentLanguage.KO : problem.getContentLanguage());
    }

    private void createNewProblemWithFollowUps(ProblemUpdateCommand command, Association<Problem> parentProblemId,
                           Association<Member> writerId, ContentLanguage contentLanguage) {
        ProblemCreateCommand createCommand = command.toCreateCommand();
        Problem created = create(createCommand, writerId, parentProblemId, Visibility.INHERITED, contentLanguage);
        createFollowUpProblems(createCommand.followUps(), created, contentLanguage);
    }

    private Problem updateExistingProblem(ProblemUpdateCommand command, Map<Long, ProblemDetailWithCategoryView> map) {
        Long problemId = command.problemId();
        ProblemDetailWithCategoryView view = map.get(problemId);
        Problem problem = view.problem();
        ProblemScoringInfo scoringInfo = view.problemScoringInfo();
        Category category = view.category();

        problem.update(Association.from(category.getId()), command.difficulty(), command.problem());
        scoringInfo.update(command.referenceAnswer());
        return problem;
    }

    private void updateFollowUps(ProblemUpdateCommand command, Map<Long, ProblemDetailWithCategoryView> map,
                                 Association<Member> writerId, Problem problem, ContentLanguage contentLanguage) {
        if (command.hasNoFollowUps()) {
            return;
        }
        for (ProblemUpdateCommand followUp : command.followUpProblems()) {
            applyUpdateRecursively(followUp, map, Association.from(problem.getId()), writerId, contentLanguage);
        }
    }
}
