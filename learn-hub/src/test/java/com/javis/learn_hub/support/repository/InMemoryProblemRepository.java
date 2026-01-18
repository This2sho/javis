package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.repository.ProblemRepository;
import com.javis.learn_hub.support.domain.Association;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.data.domain.Pageable;

public class InMemoryProblemRepository extends InMemoryRepository<Problem> implements ProblemRepository {

    @Override
    public List<Problem> findAllByParentProblemId(Association<Problem> parentProblemId) {
        return findAll(problem -> parentProblemId.equals(problem.getParentProblemId()));
    }

    @Override
    public List<Problem> findAllByIdIn(Iterable<Long> ids) {
        HashSet<Long> problemIds = new HashSet<>();
        for (Long id : ids) {
            problemIds.add(id);
        }
        return findAll(problem -> problemIds.contains(problem.getId()));
    }

    @Override
    public List<Problem> findAllByMemberIdAndParentProblemIdByLatest(LocalDateTime targetTime, Long targetId,
                                                                     Association<Member> memberId, Association<Problem> parentProblemId, Pageable pageable) {
        Predicate<Problem> cursorCondition = p ->
                p.getWriterId().equals(memberId)
                        && p.getParentProblemId().equals(parentProblemId)
                        && (p.getUpdatedAt().isBefore(targetTime)
                        || (p.getUpdatedAt().isEqual(targetTime) && p.getId() < targetId)
                );
        Comparator<Problem> latestOrder =
                Comparator.comparing(Problem::getUpdatedAt).reversed()
                        .thenComparing(Problem::getId, Comparator.reverseOrder());
        int pageSize = pageable.getPageSize();
        return findAll(cursorCondition)
                .stream()
                .sorted(latestOrder)
                .limit(pageSize)
                .toList();
    }

    @Override
    public List<Problem> findAllByMemberIdAndParentProblemIdByOldest(LocalDateTime targetTime, Long targetId,
                                                                     Association<Member> memberId, Association<Problem> parentProblemId, Pageable pageable) {
        Predicate<Problem> cursorCondition = p -> p.getWriterId().equals(memberId)
                && p.getParentProblemId().equals(parentProblemId)
                && (p.getUpdatedAt().isAfter(targetTime) ||
                (p.getUpdatedAt().isEqual(targetTime) && p.getId() > targetId)
        );
        Comparator<Problem> oldestOrder = Comparator.comparing(Problem::getUpdatedAt).thenComparing(Problem::getId);
        int pageSize = pageable.getPageSize();
        return findAll(cursorCondition)
                .stream()
                .sorted(oldestOrder)     // ORDER BY updatedAt ASC, id ASC
                .limit(pageSize)         // LIMIT size
                .toList();
    }

    @Override
    public List<Problem> findRecommendableRootProblems(List<Association<Category>> categoryIds,
                                                       Association<Problem> parentProblemId,
                                                       Association<Member> memberId) {
        return findAll(problem -> categoryIds.contains(problem.getCategoryId())
                && problem.getParentProblemId().equals(parentProblemId)
                && (
                        problem.isPublic() ||
                                (problem.isPrivate() && problem.getWriterId().equals(memberId))
                )
        );
    }
}
