package com.javis.learn_hub.problem.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.score.domain.service.CategoryRecommender;
import com.javis.learn_hub.score.domain.service.ScoreReader;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.CategoryBuilder;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.builder.ScoreBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProblemRecommenderTest {

    private final TestFixtureFactory fixtureFactory = new TestFixtureFactory();
    private final CategoryReader categoryReader = new CategoryReader(fixtureFactory.getCategoryRepository());

    private final ProblemRecommender problemRecommender = new ProblemRecommender(
        new CategoryRecommender(
                new ScoreReader(fixtureFactory.getScoreRepository(), categoryReader),
                new CategoryReader(fixtureFactory.getCategoryRepository())
        ),
            new ProblemReader(
                    fixtureFactory.getProblemRepository(),
                    fixtureFactory.getProblemScoringInfoRepository()
            ),
            fixtureFactory.getProblemRepository()
    );

    @DisplayName("주어진 메인 카테고리에서 회원 점수가 낮은 카테고리 문제 중 1가지를 가져온다.")
    @Test
    void testRecommendRootProblems() {
        //given
        Member member = fixtureFactory.make(MemberBuilder.builder().build());
        MainCategory mainCategory = MainCategory.COMPUTER_SCIENCE;
        Category category1 = fixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory).build());
        Category category2 = fixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory).build());
        Category category3 = fixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory).build());
        fixtureFactory.make(
                ScoreBuilder.builder().withScore(100).withCategoryId(category1.getId()).withMemberId(member.getId())
                        .build());
        fixtureFactory.make(
                ScoreBuilder.builder().withScore(50).withCategoryId(category2.getId()).withMemberId(member.getId())
                        .build());
        fixtureFactory.make(
                ScoreBuilder.builder().withScore(10).withCategoryId(category3.getId()).withMemberId(member.getId())
                        .build());
        fixtureFactory.make(ProblemBuilder.builder().withCategoryId(category1.getId()).build());
        fixtureFactory.make(ProblemBuilder.builder().withCategoryId(category2.getId()).build());
        Problem containProblem = fixtureFactory.make(ProblemBuilder.builder().withCategoryId(category3.getId()).build());

        int recommendSize = 1;

        //when
        List<Problem> problems = problemRecommender.recommendRootProblems(member.getId(), mainCategory, recommendSize);

        //then
        assertThat(problems).contains(containProblem);
    }

    @DisplayName("문제 추천시 원하는 개수보다 존재하는 문제 수가 작다면 존재하는 문제 수만큼만 가져온다.")
    @Test
    void testRecommendRootProblems2() {
        //given
        Member member = fixtureFactory.make(MemberBuilder.builder().build());
        MainCategory mainCategory = MainCategory.COMPUTER_SCIENCE;
        Category category1 = fixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory).build());
        Category category2 = fixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory).build());
        Category category3 = fixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory).build());
        fixtureFactory.make(
                ScoreBuilder.builder().withScore(100).withCategoryId(category1.getId()).withMemberId(member.getId())
                        .build());
        fixtureFactory.make(
                ScoreBuilder.builder().withScore(50).withCategoryId(category2.getId()).withMemberId(member.getId())
                        .build());
        fixtureFactory.make(
                ScoreBuilder.builder().withScore(10).withCategoryId(category3.getId()).withMemberId(member.getId())
                        .build());
        Problem problem1 = fixtureFactory.make(ProblemBuilder.builder().withCategoryId(category1.getId()).build());
        Problem problem2 = fixtureFactory.make(ProblemBuilder.builder().withCategoryId(category2.getId()).build());
        Problem problem3 = fixtureFactory.make(ProblemBuilder.builder().withCategoryId(category3.getId()).build());

        int recommendSize = 5;
        int problemSize = 3;

        //when
        List<Problem> problems = problemRecommender.recommendRootProblems(member.getId(), mainCategory, recommendSize);

        //then
        assertThat(problems).hasSize(problemSize);
    }

    @DisplayName("이전 문제와 선호 난이도를 기준으로 꼬리 문제를 가져온다.")
    @Test
    void testRecommendNextProblem() {
        //given
        Problem previousProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Problem relatedEasyProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.EASY).build());
        Problem nonRelatedEasyProblem = fixtureFactory.make(
                ProblemBuilder.builder().withDifficulty(Difficulty.EASY).build());
        Problem relatedMediumProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.MEDIUM).build());
        Problem relatedHardProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.HARD).build());
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        //when
        Optional<Problem> actual = problemRecommender.recommendNextProblem(Association.from(previousProblem.getId()),
                Collections.emptyList(), preferences);

        //then
        assertSoftly(at -> {
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(relatedEasyProblem);
        });
    }

    @DisplayName("이전 문제와 선호 난이도를 기준으로 꼬리 문제를 가져온다. 1순위가 없으면 다음 순위를 가져온다.")
    @Test
    void testRecommendNextProblem2() {
        //given
        Problem previousProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Problem nonRelatedEasyProblem = fixtureFactory.make(
                ProblemBuilder.builder().withDifficulty(Difficulty.EASY).build());
        Problem relatedMediumProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.MEDIUM).build());
        Problem relatedHardProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.HARD).build());
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        //when
        Optional<Problem> actual = problemRecommender.recommendNextProblem(Association.from(previousProblem.getId()),
                Collections.emptyList(), preferences);

        //then
        assertSoftly(at -> {
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(relatedMediumProblem);
        });
    }

    @DisplayName("이전 문제와 연관된 문제가 없는 경우 Optional.empty를 반환한다.")
    @Test
    void testRecommendNextProblem3() {
        //given
        Problem previousProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Problem nonRelatedEasyProblem = fixtureFactory.make(
                ProblemBuilder.builder().withDifficulty(Difficulty.EASY).build());
        Problem nonRelatedMediumProblem = fixtureFactory.make(
                ProblemBuilder.builder().withDifficulty(Difficulty.MEDIUM).build());
        Problem nonRelatedHardProblem = fixtureFactory.make(
                ProblemBuilder.builder().withDifficulty(Difficulty.HARD).build());
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        //when
        Optional<Problem> actual = problemRecommender.recommendNextProblem(Association.from(previousProblem.getId()),
                Collections.emptyList(), preferences);

        //then
        assertThat(actual).isEmpty();
    }

    @DisplayName("[인터뷰에서 이미 질문한 문제를 다시 내지 않기 위해] 문제의 id가 제외 명단에 포함된 경우 추천 문제로 내지 않는다. (연관된 모든 문제가 이미 출제되었다면 empty 반환)")
    @Test
    void testRecommendNextProblem4() {
        //given
        Problem previousProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Problem submittedEasyProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.EASY).build());
        Problem submittedMediumProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.MEDIUM).build());
        Problem submittedHardProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.HARD).build());
        List<Association<Problem>> excludeProblemIds = List.of(
                Association.from(submittedEasyProblem.getId()),
                Association.from(submittedMediumProblem.getId()),
                Association.from(submittedHardProblem.getId())
                );
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        //when
        Optional<Problem> actual = problemRecommender.recommendNextProblem(Association.from(previousProblem.getId()),
                excludeProblemIds, preferences);

        //then
        assertThat(actual).isEmpty();
    }

    @DisplayName("[인터뷰에서 이미 질문한 문제를 다시 내지 않기 위해] 문제의 id가 제외 명단에 포함된 경우 추천 문제로 내지 않는다. (1순위가 이미 제출되었으면 다음 순위 반환)")
    @Test
    void testRecommendNextProblem5() {
        //given
        Problem previousProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Problem submittedEasyProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.EASY).build());
        Problem unSubmittedMediumProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.MEDIUM).build());
        Problem unSubmittedHardProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(previousProblem.getId()).withDifficulty(Difficulty.HARD).build());
        List<Association<Problem>> excludeProblemIds = List.of(Association.from(submittedEasyProblem.getId()));
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        //when
        Optional<Problem> actual = problemRecommender.recommendNextProblem(Association.from(previousProblem.getId()),
                excludeProblemIds, preferences);

        //then
        assertSoftly(softly -> {
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(unSubmittedMediumProblem);
        });
    }
}
