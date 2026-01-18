package com.javis.learn_hub.score.service;

import com.javis.learn_hub.answer.domain.service.AnswerFinder;
import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.CategoryScoreNode;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.score.domain.service.ScoreCalculator;
import com.javis.learn_hub.score.domain.service.ScoreProcessor;
import com.javis.learn_hub.score.domain.service.ScoreReader;
import com.javis.learn_hub.score.service.dto.CategoryScoreNodeResponse;
import com.javis.learn_hub.score.service.dto.MainCategoryScoreResponse;
import com.javis.learn_hub.score.service.dto.ScoreSummaryResponse;
import com.javis.learn_hub.support.domain.Association;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ScoreService {

    private final AnswerFinder answerFinder;
    private final ScoreReader scoreReader;
    private final ScoreProcessor scoreProcessor;
    private final ScoreCalculator scoreCalculator;

    public void applyScore(Long interviewId, Long memberId) {
        List<CategoryGrade> categoryGrades = answerFinder.findCategoryGrades(Association.from(interviewId));
        Set<Score> existingScores = scoreReader.getAllBy(Association.from(memberId), categoryGrades);
        List<Score> newScores = scoreProcessor.initNewScoresByZero(existingScores, categoryGrades, Association.from(memberId));
        existingScores.addAll(newScores);
        List<Score> calculatedScores = scoreCalculator.calculate(existingScores, categoryGrades);
        scoreProcessor.updateScores(calculatedScores);
    }

    public ScoreSummaryResponse showScores(Long memberId) {
        Association<Member> member = Association.<Member>from(memberId);
        List<MainCategoryScoreResponse> scores =
                Arrays.stream(MainCategory.values())
                        .map(mainCategory ->
                                new MainCategoryScoreResponse(
                                        mainCategory,
                                        scoreReader.getMainCategoryScore(member, mainCategory)
                                )
                        )
                        .toList();

        return new ScoreSummaryResponse(scores);
    }

    public CategoryScoreNodeResponse showDetailScore(Long memberId, String mainCategoryName) {
        Map<Category, Integer> allSubCategoryScores = scoreReader.getAllSubCategoryScores(Association.from(memberId),
                MainCategory.from(mainCategoryName));
        CategoryScoreNode categoryScoreNode = CategoryScoreNode.from(allSubCategoryScores);
        return CategoryScoreNodeResponse.from(categoryScoreNode);
    }
}
