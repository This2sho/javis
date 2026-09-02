package com.javis.learn_hub.problem.domain.repository;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.i18n.ContentLanguage;
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

    @Query(value = """
    SELECT p.*
    FROM problem p
    JOIN category c ON c.id = p.category_id
    WHERE p.writer_id = :memberId
      AND p.parent_problem_id = -1
      AND (:mainCategoryPath IS NULL OR c.path LIKE CONCAT(:mainCategoryPath, ':%') OR c.path = :mainCategoryPath)
      AND (:rootCategoryPath IS NULL OR c.path = :rootCategoryPath OR c.path LIKE CONCAT(:rootCategoryPath, ':%'))
      AND (
            p.content_language = :contentLanguage
            OR (:includeNullLanguage = true AND p.content_language IS NULL)
          )
      AND (p.updated_at < :targetTime OR (p.updated_at = :targetTime AND p.id <= :targetId))
    ORDER BY p.updated_at DESC, p.id DESC
    """, nativeQuery = true)
    List<Problem> findAllRootByMemberAndFiltersByLatest(
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            @Param("memberId") Long memberId,
            @Param("mainCategoryPath") String mainCategoryPath,
            @Param("rootCategoryPath") String rootCategoryPath,
            @Param("contentLanguage") String contentLanguage,
            @Param("includeNullLanguage") boolean includeNullLanguage,
            Pageable pageable
    );

    @Query(value = """
    SELECT p.*
    FROM problem p
    JOIN category c ON c.id = p.category_id
    WHERE p.writer_id = :memberId
      AND p.parent_problem_id = -1
      AND (:mainCategoryPath IS NULL OR c.path LIKE CONCAT(:mainCategoryPath, ':%') OR c.path = :mainCategoryPath)
      AND (:rootCategoryPath IS NULL OR c.path = :rootCategoryPath OR c.path LIKE CONCAT(:rootCategoryPath, ':%'))
      AND (
            p.content_language = :contentLanguage
            OR (:includeNullLanguage = true AND p.content_language IS NULL)
          )
      AND (p.updated_at > :targetTime OR (p.updated_at = :targetTime AND p.id >= :targetId))
    ORDER BY p.updated_at ASC, p.id ASC
    """, nativeQuery = true)
    List<Problem> findAllRootByMemberAndFiltersByOldest(
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            @Param("memberId") Long memberId,
            @Param("mainCategoryPath") String mainCategoryPath,
            @Param("rootCategoryPath") String rootCategoryPath,
            @Param("contentLanguage") String contentLanguage,
            @Param("includeNullLanguage") boolean includeNullLanguage,
            Pageable pageable
    );

    Optional<Problem> findById(Long problemId);

    void deleteById(Long problemId);

    @Query("""
        select p
        from Problem p
        where p.categoryId in :categoryIds
          and p.parentProblemId = :parentProblemId
          and (
                p.contentLanguage = :contentLanguage
             or (:includeNullLanguage = true and p.contentLanguage is null)
          )
          and (
                p.visibility = com.javis.learn_hub.problem.domain.Visibility.PUBLIC
             or (p.visibility = com.javis.learn_hub.problem.domain.Visibility.PRIVATE and p.writerId = :memberId)
          )
    """)
    List<Problem> findRecommendableRootProblems(
            @Param("categoryIds") List<Association<Category>> categoryIds,
            @Param("parentProblemId") Association<Problem> parentProblemId,
            @Param("memberId") Association<Member> memberId,
            @Param("contentLanguage") ContentLanguage contentLanguage,
            @Param("includeNullLanguage") boolean includeNullLanguage
    );
}
