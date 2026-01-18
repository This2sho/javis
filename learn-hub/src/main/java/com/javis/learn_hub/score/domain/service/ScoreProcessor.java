package com.javis.learn_hub.score.domain.service;

import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.score.domain.repository.ScoreRepository;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ScoreProcessor {

    private final ScoreRepository scoreRepository;

    public void

    updateScores(List<Score> scores) {
        scoreRepository.saveAll(scores);
    }

    public List<Score> initNewScoresByZero(Set<Score> existingScores, List<CategoryGrade> categoryGrades, Association<Member> memberId) {
        Map<Long, Score> scoresByCategoryId = collectScoresByCategoryId(existingScores);
        Set<Long> categoryIds = collectToCategoryIds(categoryGrades);
        List<Score> newScores = makeNewScores(memberId, categoryIds, scoresByCategoryId);
        scoreRepository.saveAll(newScores);
        return newScores;
    }

    private Map<Long, Score> collectScoresByCategoryId(Set<Score> existingScores) {
        return existingScores.stream()
                .collect(Collectors.toMap(score -> score.getCategoryId().getId(), score -> score));
    }

    private Set<Long> collectToCategoryIds(List<CategoryGrade> categoryGrades) {
        return categoryGrades.stream().map(CategoryGrade::categoryId).collect(Collectors.toSet());
    }

    private List<Score> makeNewScores(Association<Member> memberId, Set<Long> categoryIds,
                                      Map<Long, Score> scoresByCategoryId) {
        return categoryIds.stream()
                .filter(categoryId -> !scoresByCategoryId.containsKey(categoryId))
                .map(categoryId -> new Score(memberId, Association.from(categoryId)))
                .toList();
    }
}
