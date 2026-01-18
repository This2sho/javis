package com.javis.learn_hub.problem.domain.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.dto.ProblemDetailView;
import com.javis.learn_hub.problem.domain.service.dto.ProblemDetailWithCategoryView;
import com.javis.learn_hub.problem.domain.service.dto.ProblemTreeView;
import com.javis.learn_hub.problem.domain.service.dto.ProblemUpdateCommand;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.CategoryBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.builder.ProblemScoringInfoBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProblemFinderTest {

    private final TestFixtureFactory testFixtureFactory = new TestFixtureFactory();
    private final ProblemFinder problemFinder = new ProblemFinder(
            new ProblemReader(
                    testFixtureFactory.getProblemRepository(),
                    testFixtureFactory.getProblemScoringInfoRepository()
            ),
            new CategoryReader(
                    testFixtureFactory.getCategoryRepository()
            )
    );

    @DisplayName("문제들의 모든 카테고리 정보를 문제 id로 묶어서 가져온다.")
    @Test
    void testGetAllCategory() {
        //given
        Category category1 = testFixtureFactory.make(CategoryBuilder.builder().withMainCategory(MainCategory.COMPUTER_SCIENCE).build());
        Category category2 = testFixtureFactory.make(CategoryBuilder.builder().withMainCategory(MainCategory.BACKEND).build());
        Problem problem1 = testFixtureFactory.make(ProblemBuilder.builder().withCategoryId(category1.getId()).build());
        Problem problem2 = testFixtureFactory.make(ProblemBuilder.builder().withCategoryId(category2.getId()).build());
        List<Problem> problems = List.of(problem1, problem2);

        //when
        Map<Long, Category> actual = problemFinder.getAllCategory(problems);

        //then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).hasSize(2);
            softAssertions.assertThat(actual.get(problem1.getId())).isEqualTo(category1);
            softAssertions.assertThat(actual.get(problem2.getId())).isEqualTo(category2);
        });
    }

    @DisplayName("문제 id로 문제들과 관련된 모든 정보를 트리형태로 가져온다.")
    @Test
    void testFindProblemTree() {
        //given
        Category category = testFixtureFactory.make(CategoryBuilder.builder().build());
        Problem problem = testFixtureFactory.make(ProblemBuilder.builder().withCategoryId(category.getId()).build());
        ProblemScoringInfo scoringInfo = testFixtureFactory.make(
                ProblemScoringInfoBuilder.builder().withProblemId(problem.getId()).build());
        Problem childProblem = testFixtureFactory.make(ProblemBuilder.builder().withParentProblemId(problem.getId()).build());
        ProblemScoringInfo childScoringInfo = testFixtureFactory.make(
                ProblemScoringInfoBuilder.builder().withProblemId(childProblem.getId()).build());

        //when
        ProblemTreeView actual = problemFinder.findProblemTree(problem.getId());

        //then
        assertSoftly(softAssertions -> {
            Problem actualProblem = actual.problemDetailWithCategoryView().problem();
            Category actualCategory = actual.problemDetailWithCategoryView().category();
            ProblemScoringInfo actualScoringInfo = actual.problemDetailWithCategoryView().problemScoringInfo();
            List<ProblemTreeView> children = actual.children();
            softAssertions.assertThat(actualProblem).isEqualTo(problem);
            softAssertions.assertThat(actualCategory).isEqualTo(category);
            softAssertions.assertThat(actualScoringInfo.getId()).isEqualTo(scoringInfo.getId());
            softAssertions.assertThat(children).hasSize(1);
            softAssertions.assertThat(children.get(0).problemDetailWithCategoryView().problem())
                    .isEqualTo(childProblem);
        });
    }

    @DisplayName("문제 id로 문제 정보와 문제 채점 관련 정보를 가져온다.")
    @Test
    void testFindProblemDetail() {
        //given
        Category category = testFixtureFactory.make(CategoryBuilder.builder().build());
        Problem problem = testFixtureFactory.make(ProblemBuilder.builder().withCategoryId(category.getId()).build());
        ProblemScoringInfo scoringInfo = testFixtureFactory.make(
                ProblemScoringInfoBuilder.builder().withProblemId(problem.getId()).build());

        //when
        ProblemDetailView actual = problemFinder.findProblemDetail(problem.getId());

        //then
        assertSoftly(softAssertions -> {
            Problem actualProblem = actual.problem();
            ProblemScoringInfo actualScoringInfo = actual.problemScoringInfo();
            softAssertions.assertThat(actualProblem).isEqualTo(problem);
            softAssertions.assertThat(actualScoringInfo.getId()).isEqualTo(scoringInfo.getId());
        });
    }

    @DisplayName("문제를 업데이트 정보에 관련된 모든 문제들을 문제 id로 묶어서 가져온다.")
    @Test
    void testFindAll() {
        //given
        Category category = testFixtureFactory.make(CategoryBuilder.builder().build());
        Problem problem = testFixtureFactory.make(ProblemBuilder.builder().withCategoryId(category.getId()).build());
        ProblemScoringInfo scoringInfo = testFixtureFactory.make(
                ProblemScoringInfoBuilder.builder().withProblemId(problem.getId()).build());
        Problem childProblem = testFixtureFactory.make(ProblemBuilder.builder().withParentProblemId(problem.getId()).withCategoryId(category.getId()).build());
        ProblemScoringInfo childScoringInfo = testFixtureFactory.make(
                ProblemScoringInfoBuilder.builder().withProblemId(childProblem.getId()).build());

        ProblemUpdateCommand childCommand = new ProblemUpdateCommand(childProblem.getId(),
                childProblem.getContent(), childScoringInfo.getReferenceAnswer(), childScoringInfo.getKeywords(),
                childProblem.getDifficulty(),
                category.getPath(), Collections.emptyList());
        ProblemUpdateCommand command = new ProblemUpdateCommand(problem.getId(),
                problem.getContent(), scoringInfo.getReferenceAnswer(), scoringInfo.getKeywords(),
                problem.getDifficulty(),
                category.getPath(), List.of(childCommand));
        //when
        Map<Long, ProblemDetailWithCategoryView> actual = problemFinder.findAll(command);

        //then
        assertSoftly(softAssertions -> {
            Problem actualProblem = actual.get(problem.getId()).problem();
            ProblemScoringInfo actualScoringInfo = actual.get(problem.getId()).problemScoringInfo();
            Category actualCategory = actual.get(problem.getId()).category();
            Problem actualChildProblem = actual.get(childProblem.getId()).problem();
            ProblemScoringInfo actualChildScoringInfo = actual.get(childProblem.getId()).problemScoringInfo();
            softAssertions.assertThat(actualProblem).isEqualTo(problem);
            softAssertions.assertThat(actualCategory).isEqualTo(category);
            softAssertions.assertThat(actualScoringInfo).isEqualTo(scoringInfo);
            softAssertions.assertThat(actualChildProblem).isEqualTo(childProblem);
            softAssertions.assertThat(actualChildScoringInfo).isEqualTo(childScoringInfo);
        });
    }
}
