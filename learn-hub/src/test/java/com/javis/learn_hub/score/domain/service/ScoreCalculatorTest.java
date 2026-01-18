package com.javis.learn_hub.score.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.javis.learn_hub.answer.domain.Grade;
import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScoreCalculatorTest {

    private final ScoreCalculator scoreCalculator = new ScoreCalculator();

    @DisplayName("점수가 없던 카테고리에 Perfetct 등급을 계산하면 점수는 Perfect의 score가 된다.")
    @Test
    void testCalculate1() {
        //given
        Association<Member> memberId = Association.from(1L);
        Long categoryId = 1L;
        Grade perfect = Grade.PERFECT;
        Score newScore = new Score(memberId, Association.from(categoryId));
        CategoryGrade categoryGrade = new CategoryGrade(categoryId, perfect);
        int expected = perfect.getScore();

        //when
        Score actual = scoreCalculator.calculate(Set.of(newScore), List.of(categoryGrade)).get(0);

        //then
        assertThat(actual.getScore()).isEqualTo(expected);
    }

    @DisplayName("점수가 10점이던 카테고리에 Perfetct 등급을 계산하면 기존 점수에서 Perfect의 score가 더 해진다.")
    @Test
    void testCalculate2() {
        //given
        Association<Member> memberId = Association.from(1L);
        Long categoryId = 1L;
        Grade perfect = Grade.PERFECT;
        Score tenScore = new Score(memberId, Association.from(categoryId));
        tenScore.addScore(10);

        CategoryGrade categoryGrade = new CategoryGrade(categoryId, perfect);
        int expected = perfect.getScore() + 10;

        //when
        Score actual = scoreCalculator.calculate(Set.of(tenScore), List.of(categoryGrade)).get(0);

        //then
        assertThat(actual.getScore()).isEqualTo(expected);
    }

    @DisplayName("0점 두개의 카테고리에 Perfect, Good 등급을 계산하면 각 등급의 score가 된다.")
    @Test
    void testCalculate3() {
        //given
        Association<Member> memberId = Association.from(1L);
        Long firstCategoryId = 1L;
        Long secondCategoryId = 2L;

        Grade firstGrade = Grade.PERFECT;
        Grade secondGrade = Grade.GOOD;
        Score firstScore = new Score(memberId, Association.from(firstCategoryId));
        Score secondScore = new Score(memberId, Association.from(secondCategoryId));

        CategoryGrade firstCategoryGrade = new CategoryGrade(firstCategoryId, firstGrade);
        CategoryGrade secondCategoryGrade = new CategoryGrade(secondCategoryId, secondGrade);

        int firstExpected = firstGrade.getScore();
        int secondExpected = secondGrade.getScore();

        //when
        List<Score> actual = scoreCalculator.calculate(Set.of(firstScore, secondScore),
                List.of(firstCategoryGrade, secondCategoryGrade));

        //then
        assertSoftly(
                at -> {
                    assertThat(actual.size()).isEqualTo(2);
                    assertThat(firstScore.getScore()).isEqualTo(firstExpected);
                    assertThat(secondScore.getScore()).isEqualTo(secondExpected);
                }
        );
    }
}
