package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.score.domain.repository.ScoreRepository;
import com.javis.learn_hub.support.domain.Association;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InMemoryScoreRepository extends InMemoryRepository<Score> implements ScoreRepository {

    @Override
    public Set<Score> findByMemberIdAndCategoryIdIn(Association<Member> memberId,
                                                    List<Association<Category>> categoryIds) {
        return new HashSet<>(findAll(
                score -> score.getMemberId().equals(memberId) && categoryIds.contains(score.getCategoryId())));
    }

    @Override
    public int sumScoresByMemberIdAndCategoryIdIn(Association<Member> memberId,
                                                  List<Association<Category>> categoryIds) {
        List<Score> scores = findAll(s -> s.getMemberId().equals(memberId) && categoryIds.contains(s.getCategoryId()));
        return scores.stream()
                .mapToInt(Score::getScore)
                .sum();
    }
}
