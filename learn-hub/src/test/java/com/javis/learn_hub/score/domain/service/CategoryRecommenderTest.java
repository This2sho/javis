package com.javis.learn_hub.score.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.support.SimpleCategoryReader;
import com.javis.learn_hub.support.SimpleScoreReader;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.CategoryBuilder;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ScoreBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryRecommenderTest {

    private final TestFixtureFactory testFixtureFactory = new TestFixtureFactory();
    private final CategoryReader categoryReader = new SimpleCategoryReader(testFixtureFactory.getCategoryRepository());
    private final CategoryRecommender categoryRecommender = new CategoryRecommender(
            new SimpleScoreReader(testFixtureFactory.getScoreRepository(), categoryReader),
            categoryReader
    );

    @DisplayName("메인 카테고리의 하위 카테고리 중 점수가 낮은 하위 카테고리들의 아이디를 반환한다.")
    @Test
    void testRecommendCategoryIdsByScore() {
        //given
        MainCategory computerScience = MainCategory.COMPUTER_SCIENCE;
        Category category1 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory1").build());
        Category category2 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory2").build());
        Category category3 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory3").build());
        Category category4 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory4").build());
        Member member = testFixtureFactory.make(MemberBuilder.builder().build());
        testFixtureFactory.make(
                ScoreBuilder.builder().withMemberId(member.getId()).withCategoryId(category1.getId()).withScore(0).build());
        testFixtureFactory.make(
                ScoreBuilder.builder().withMemberId(member.getId()).withCategoryId(category2.getId()).withScore(1).build());
        testFixtureFactory.make(
                ScoreBuilder.builder().withMemberId(member.getId()).withCategoryId(category3.getId()).withScore(10).build());
        testFixtureFactory.make(
                ScoreBuilder.builder().withMemberId(member.getId()).withCategoryId(category4.getId()).withScore(20).build());

        List<Association<Category>> expectedCategoryIds = List.of(
                Association.from(category1.getId()), Association.from(category2.getId()), Association.from(category3.getId()));
        int recommendSize = 3;

        //when
        List<Association<Category>> categoryIds = categoryRecommender.recommendCategoryIdsByScore(
                computerScience, member.getId(), recommendSize);

        //then
        assertThat(categoryIds).containsAll(expectedCategoryIds);
    }

    @DisplayName("[기존 점수가 추천 개수 이하일 경우] 점수가 아직 생성되지 않은 하위 카테고리를 추가해서 가져온다.")
    @Test
    void testRecommendCategoryIdsByScore2() {
        //given
        MainCategory computerScience = MainCategory.COMPUTER_SCIENCE;
        Category category1 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory1").build());
        Category category2 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory2").build());
        Category category3 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory3").build());
        Member member = testFixtureFactory.make(MemberBuilder.builder().build());
        testFixtureFactory.make(
                ScoreBuilder.builder().withMemberId(member.getId()).withCategoryId(category1.getId()).withScore(10).build());

        List<Association<Category>> expectedCategoryIds = List.of(
                Association.from(category1.getId()), Association.from(category2.getId()), Association.from(category3.getId()));
        int recommendSize = 3;

        //when
        List<Association<Category>> categoryIds = categoryRecommender.recommendCategoryIdsByScore(
                computerScience, member.getId(), recommendSize);

        //then
        assertThat(categoryIds).containsAll(expectedCategoryIds);
    }

    @DisplayName("[시도하지 않은 카테고리가 있는 경우] 0점인 카테고리를 우선으로 가져온다.")
    @Test
    void testRecommendCategoryIdsByScore3() {
        //given
        MainCategory computerScience = MainCategory.COMPUTER_SCIENCE;
        Category existingCategory1 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory1").build());
        Category existingCategory2 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory2").build());
        Category firstTryCategory1 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory3").build());
        Category firstTryCategory2 = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory4").build());
        Member member = testFixtureFactory.make(MemberBuilder.builder().build());
        testFixtureFactory.make(
                ScoreBuilder.builder().withMemberId(member.getId()).withCategoryId(existingCategory1.getId()).withScore(10).build());
        testFixtureFactory.make(
                ScoreBuilder.builder().withMemberId(member.getId()).withCategoryId(existingCategory2.getId()).withScore(10).build());

        List<Association<Category>> expectedCategoryIds = List.of(
                Association.from(firstTryCategory1.getId()), Association.from(firstTryCategory2.getId()));
        int recommendSize = 2;

        //when
        List<Association<Category>> categoryIds = categoryRecommender.recommendCategoryIdsByScore(
                computerScience, member.getId(), recommendSize);

        //then
        SoftAssertions.assertSoftly(softAssertions -> {
                    assertThat(categoryIds).hasSize(recommendSize);
                    assertThat(categoryIds).containsAll(expectedCategoryIds);
            }
        );
    }

    @DisplayName("[하위 카테고리가 추천 수보다 작은 경우] 존재하는 하위 카테고리 수만큼만 가져온다.")
    @Test
    void testRecommendCategoryIdsByScore4() {
        //given
        MainCategory computerScience = MainCategory.COMPUTER_SCIENCE;
        Category existingCategory = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory1").build());
        Category firstTryCategory = testFixtureFactory.make(
                CategoryBuilder.builder().withMainCategory(computerScience).withSubCategories("subCategory2").build());

        Member member = testFixtureFactory.make(MemberBuilder.builder().build());
        testFixtureFactory.make(
                ScoreBuilder.builder().withMemberId(member.getId()).withCategoryId(existingCategory.getId()).withScore(10).build());

        List<Association<Category>> expectedCategoryIds = List.of(
                Association.from(existingCategory.getId()), Association.from(firstTryCategory.getId()));
        int recommendSize = 5;

        //when
        List<Association<Category>> categoryIds = categoryRecommender.recommendCategoryIdsByScore(
                computerScience, member.getId(), recommendSize);

        //then
        SoftAssertions.assertSoftly(softAssertions -> {
                    assertThat(categoryIds).hasSize(expectedCategoryIds.size());
                    assertThat(categoryIds).containsAll(expectedCategoryIds);
                }
        );
    }
}
