package com.javis.learn_hub.review.domain.repository;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.support.domain.Association;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends Repository<Review, Long> {

    Review save(Review review);

    @Query("""
    SELECT r
    FROM Review r
    WHERE r.revieweeId = :revieweeId
            AND (r.updatedAt < :targetTime OR (r.updatedAt = :targetTime AND r.id <= :targetId))
    ORDER BY r.updatedAt DESC, r.id DESC
    """)
    List<Review> findAllByRevieweeIdByLatest(
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            Association<Member> revieweeId,
            Pageable pageable
    );

    @Query("""
    SELECT r
    FROM Review r
    WHERE r.revieweeId = :revieweeId
            AND (r.updatedAt > :targetTime OR (r.updatedAt = :targetTime AND r.id >= :targetId))
    ORDER BY r.updatedAt ASC, r.id ASC
    """)
    List<Review> findAllByRevieweeIdByOldest(
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            Association<Member> revieweeId,
            Pageable pageable
    );

    @Query("""
    SELECT r
    FROM Review r
    WHERE r.registrationStatus = :registrationStatus
            AND (r.updatedAt < :targetTime OR (r.updatedAt = :targetTime AND r.id <= :targetId))
    ORDER BY r.updatedAt DESC, r.id DESC
    """)
    List<Review> findAllByRegistrationStatusByLatest(
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            RegistrationStatus registrationStatus,
            Pageable pageable
    );

    @Query("""
    SELECT r
    FROM Review r
    WHERE r.registrationStatus = :registrationStatus
            AND (r.updatedAt > :targetTime OR (r.updatedAt = :targetTime AND r.id >= :targetId))
    ORDER BY r.updatedAt ASC, r.id ASC
    """)
    List<Review> findAllByRegistrationStatusByOldest(
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            RegistrationStatus registrationStatus,
            Pageable pageable
    );

    Optional<Review> findById(Long reviewId);

    boolean existsByRootProblemId(Association<Problem> rootProblemId);
}
