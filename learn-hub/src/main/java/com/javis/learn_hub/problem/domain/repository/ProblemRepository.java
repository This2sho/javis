package com.javis.learn_hub.problem.domain.repository;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.support.domain.Association;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends Repository<Problem, Long> {

    Problem save(Problem problem);

    List<Problem> findAllByParentProblemId(Association<Problem> parentProblemId);

    List<Problem> findAllByIdIn(Iterable<Long> ids);

    @Query("""
    SELECT p
    FROM Problem p
    WHERE p.writerId = :memberId AND p.parentProblemId = :parentProblemId
            AND (p.updatedAt < :targetTime OR (p.updatedAt = :targetTime AND p.id <= :targetId))
    ORDER BY p.updatedAt DESC, p.id DESC
    """)
    List<Problem> findAllByMemberIdAndParentProblemIdByLatest(
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            Association<Member> memberId,
            Association<Problem> parentProblemId,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Problem p
    WHERE p.writerId = :memberId AND p.parentProblemId = :parentProblemId
            AND (p.updatedAt > :targetTime OR (p.updatedAt = :targetTime AND p.id >= :targetId))
    ORDER BY p.updatedAt ASC, p.id ASC
    """)
    List<Problem> findAllByMemberIdAndParentProblemIdByOldest(
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            Association<Member> memberId,
            Association<Problem> parentProblemId,
            Pageable pageable
    );

    Optional<Problem> findById(Long problemId);

    @Query("""
        select p
        from Problem p
        where p.categoryId in :categoryIds
          and p.parentProblemId = :parentProblemId
          and (
                p.visibility = com.javis.learn_hub.problem.domain.Visibility.PUBLIC
             or (p.visibility = com.javis.learn_hub.problem.domain.Visibility.PRIVATE and p.writerId = :memberId)
          )
    """)
    List<Problem> findRecommendableRootProblems(
            @Param("categoryIds") List<Association<Category>> categoryIds,
            @Param("parentProblemId") Association<Problem> parentProblemId,
            @Param("memberId") Association<Member> memberId
    );
}
