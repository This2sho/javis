package com.javis.learn_hub.interview.domain.repository;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.InterviewStatus;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.support.domain.Association;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface InterviewRepository extends Repository<Interview, Long> {

    Optional<Interview> findById(Long id);

    Interview save(Interview interview);

    @Query("""
    SELECT i
    FROM Interview i
    WHERE i.status = :interviewStatus AND i.memberId = :memberId
            AND (i.updatedAt < :targetTime OR (i.updatedAt = :targetTime AND i.id <= :targetId))
    ORDER BY i.updatedAt DESC, i.id DESC
    """)
    List<Interview> findAllByInterviewStatusAndMemberIdByLatest(
            InterviewStatus interviewStatus,
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            Association<Member> memberId,
            Pageable pageable
    );

    @Query("""
    SELECT i
    FROM Interview i
    WHERE i.status = :interviewStatus AND i.memberId = :memberId
            AND (i.updatedAt > :targetTime OR (i.updatedAt = :targetTime AND i.id >= :targetId))
    ORDER BY i.updatedAt ASC, i.id ASC
    """)
    List<Interview> findAllByInterviewStatusAndMemberIdByOldest(
            InterviewStatus interviewStatus,
            @Param("targetTime") LocalDateTime targetTime,
            @Param("targetId") Long targetId,
            Association<Member> memberId,
            Pageable pageable
    );

    Optional<Interview> findByMemberIdAndMainCategoryAndStatus(
            Association<Member> memberId,
            MainCategory mainCategory,
            InterviewStatus interviewStatus
    );
}
