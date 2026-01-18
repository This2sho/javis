package com.javis.learn_hub.score.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.javis.learn_hub.answer.domain.Grade;
import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.CategoryBuilder;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ScoreBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScoreProcessorTest {

    private final TestFixtureFactory testFixtureFactory = new TestFixtureFactory();
    private final ScoreProcessor scoreProcessor = new ScoreProcessor(testFixtureFactory.getScoreRepository());

    @DisplayName("채점 목록에서 기존에 존재하지 않던 점수들을 등록한다.")
    @Test
    void testInitNewScoresByZero() {
        //given
        Member member = testFixtureFactory.make(MemberBuilder.builder().build());
        Category existingCategory = testFixtureFactory.make(CategoryBuilder.builder().build());
        Category newCategory = testFixtureFactory.make(CategoryBuilder.builder().build());
        Score existingScore = testFixtureFactory.make(ScoreBuilder.builder().withCategoryId(existingCategory.getId()).build());

        Set<Score> existingScores = Set.of(existingScore);
        List<CategoryGrade> categoryGrades = List.of(new CategoryGrade(existingCategory.getId(), Grade.GOOD), new CategoryGrade(
                newCategory.getId(), Grade.GOOD));

        //when
        List<Score> newScores = scoreProcessor.initNewScoresByZero(existingScores, categoryGrades, Association.from(member.getId()));

        //then
        assertSoftly(at -> {
            assertThat(newScores).doesNotContainAnyElementsOf(existingScores);
            assertThat(newScores).anyMatch(score -> score.getCategoryId().equals(Association.from(newCategory.getId())));
        });
    }
}
