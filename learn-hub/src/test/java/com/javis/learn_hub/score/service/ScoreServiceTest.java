package com.javis.learn_hub.score.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.evaluation.domain.Grade;
import com.javis.learn_hub.answer.domain.service.AnswerFinder;
import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.score.domain.service.ScoreCalculator;
import com.javis.learn_hub.score.domain.service.ScoreProcessor;
import com.javis.learn_hub.score.domain.service.ScoreReader;
import com.javis.learn_hub.support.builder.ScoreBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ScoreServiceTest {

    @MockitoBean
    private AnswerFinder answerFinder;

    @MockitoBean
    private ScoreReader scoreReader;

    @MockitoBean
    private ScoreProcessor scoreProcessor;

    @MockitoBean
    private ScoreCalculator scoreCalculator;

    @Autowired
    private ScoreService scoreService;

    @DisplayName("인터뷰 종료 후 점수를 계산하고 결과를 반영한다.")
    @Test
    void testApplyScore() {
        //given
        Long interviewId = 1L;
        Long memberId = 1L;
        List<CategoryGrade> categoryGrades = List.of(
                new CategoryGrade(1L, Grade.PERFECT),
                new CategoryGrade(2L, Grade.GOOD));
        given(answerFinder.findCategoryGrades(any()))
                .willReturn(categoryGrades);

        Set<Score> existingScore = new HashSet<>(Set.of(
                ScoreBuilder.builder().withMemberId(memberId).withCategoryId(1L).withScore(10).build()));
        given(scoreReader.getAllBy(Association.from(memberId), categoryGrades))
                .willReturn(existingScore);

        List<Score> newScores = List.of(ScoreBuilder.builder().withMemberId(memberId).withCategoryId(2L).build());
        given(scoreProcessor.initNewScoresByZero(existingScore, categoryGrades, Association.from(memberId)))
                .willReturn(newScores);

        //when
        scoreService.applyScore(interviewId, memberId);

        //then
        verify(scoreProcessor, atLeastOnce()).updateScores(any());
    }
}
