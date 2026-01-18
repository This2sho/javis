package com.javis.learn_hub.score.domain.repository;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface ScoreRepository extends Repository<Score, Long> {

    void saveAll(Iterable<Score> scores);

    Set<Score> findByMemberIdAndCategoryIdIn(
            Association<Member> memberId,
            List<Association<Category>> categoryIds
    );

    @Query("""
        SELECT COALESCE(SUM(s.score), 0)
        FROM Score s
        WHERE s.memberId = :memberId
          AND s.categoryId IN :categoryIds
    """)
    int sumScoresByMemberIdAndCategoryIdIn(
            Association<Member> memberId,
            List<Association<Category>> categoryIds
    );
}
