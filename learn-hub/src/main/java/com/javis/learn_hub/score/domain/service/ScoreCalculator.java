package com.javis.learn_hub.score.domain.service;

import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.score.domain.Score;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ScoreCalculator {

    public List<Score> calculate(Set<Score> scores, List<CategoryGrade> categoryGrades) {
        Map<Long, Score> scoresByCategoryId = collectByCategoryId(scores);
        return categoryGrades.stream()
                .map(categoryGrade -> {
                    Score score = scoresByCategoryId.get(categoryGrade.categoryId());
                    score.addScore(categoryGrade.grade().getScore());
                    return score;
                }).toList();
    }

    private Map<Long, Score> collectByCategoryId(Set<Score> scores) {
        return scores.stream()
                .collect(Collectors.toMap(score -> score.getCategoryId().getId(), score -> score));
    }
}
