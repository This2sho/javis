package com.javis.learn_hub.problem.domain.service;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.service.CategoryProcessor;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Keywords;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.Visibility;
import com.javis.learn_hub.problem.domain.service.dto.ProblemCreateCommand;
import com.javis.learn_hub.problem.domain.service.dto.ProblemUpdateCommand;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.CategoryBuilder;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.builder.ProblemScoringInfoBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProblemProcessorTest {

    private TestFixtureFactory testFixtureFactory = new  TestFixtureFactory();
    private ProblemReader problemReader = new ProblemReader(
            testFixtureFactory.getProblemRepository(),
            testFixtureFactory.getProblemScoringInfoRepository()
    );
    private ProblemProcessor problemProcessor = new ProblemProcessor(
            testFixtureFactory.getProblemRepository(),
            testFixtureFactory.getProblemScoringInfoRepository(),
            new ProblemFinder(
                    problemReader,
                    new CategoryReader(
                            testFixtureFactory.getCategoryRepository()
                    )
            ),
            new CategoryProcessor(
                    testFixtureFactory.getCategoryRepository()
            )
    );

    @DisplayName("문제 생성 커맨드, 사용자 id, 가시성 정보를 통해서 문제와 이하 꼬리 문제를 생성한다.")
    @Test
    void testCreate() {
        //given
        Difficulty followUpDifficulty = Difficulty.MEDIUM;
        ProblemCreateCommand followUpCommand = new ProblemCreateCommand(
                "꼬리 문제",
                "꼬리 문제 예상 답변",
                Keywords.from(Set.of("꼬리 문제 키워드1", "꼬리 문제 키워드2")),
                followUpDifficulty,
                "COMPUTER_SCIENCE:NETWORK",
                Collections.emptyList()
        );
        Difficulty problemDifficulty = Difficulty.EASY;
        ProblemCreateCommand command = new ProblemCreateCommand(
                "문제",
                "문제 예상 답변",
                Keywords.from(Set.of("문제 키워드1", "문제 키워드2")),
                problemDifficulty,
                "COMPUTER_SCIENCE:NETWORK",
                List.of(followUpCommand)
        );
        Member writer = testFixtureFactory.make(MemberBuilder.builder().build());
        Visibility visibility = Visibility.PRIVATE;

        //when
        Problem problem = problemProcessor.create(command, writer.getId(), visibility);
        List<Problem> followUpProblems = problemReader.getFollowUpProblems(Association.from(problem.getId()));

        //then
        SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(problem.getContent()).isEqualTo(command.problem());
                    softly.assertThat(problem.getDifficulty()).isEqualTo(problemDifficulty);
                    softly.assertThat(followUpProblems).hasSize(1);
                    Problem followUpProblem = followUpProblems.get(0);
                    softly.assertThat(followUpProblem.getContent()).isEqualTo(followUpCommand.problem());
                    softly.assertThat(followUpProblem.getDifficulty()).isEqualTo(followUpDifficulty);
                }
        );
    }

    @DisplayName("[update: 루트 문제, 꼬리 문제 모두 존재 상황] 문제 업데이트 커맨드, 사용자 id를 통해서 문제와 이하 꼬리 문제를 업데이트한다.")
    @Test
    void testUpdate() {
        //given
        Member writer = testFixtureFactory.make(MemberBuilder.builder().build());
        Category category = testFixtureFactory.make(CategoryBuilder.builder().build());
        Problem problem = testFixtureFactory.make(
                ProblemBuilder.builder().withCategoryId(category.getId()).withWriterId(writer.getId()).withDifficulty(Difficulty.HARD).build());
        testFixtureFactory.make(ProblemScoringInfoBuilder.builder().withProblemId(problem.getId()).build());
        Problem followUpProblem = testFixtureFactory.make(
                ProblemBuilder.builder().withCategoryId(category.getId()).withParentProblemId(problem.getId()).withWriterId(writer.getId()).withDifficulty(Difficulty.HARD).build());
        testFixtureFactory.make(ProblemScoringInfoBuilder.builder().withProblemId(followUpProblem.getId()).build());


        Difficulty followUpDifficulty = Difficulty.MEDIUM;
        ProblemUpdateCommand followUpCommand = new ProblemUpdateCommand(
                followUpProblem.getId(),
                "꼬리 문제",
                "꼬리 문제 예상 답변",
                Keywords.from(Set.of("꼬리 문제 키워드1", "꼬리 문제 키워드2")),
                followUpDifficulty,
                category.getPath(),
                Collections.emptyList()
        );
        Difficulty problemDifficulty = Difficulty.EASY;
        ProblemUpdateCommand command = new ProblemUpdateCommand(
                problem.getId(),
                "문제",
                "문제 예상 답변",
                Keywords.from(Set.of("문제 키워드1", "문제 키워드2")),
                problemDifficulty,
                category.getPath(),
                List.of(followUpCommand)
        );

        //when
        problemProcessor.update(command, Association.from(writer.getId()));
        Problem actualProblem = problemReader.get(problem.getId());
        List<Problem> followUpProblems = problemReader.getFollowUpProblems(Association.from(actualProblem.getId()));

        //then
        SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(actualProblem.getContent()).isEqualTo(command.problem());
                    softly.assertThat(actualProblem.getDifficulty()).isEqualTo(problemDifficulty);
                    softly.assertThat(followUpProblems).hasSize(1);
                    Problem actualFollowUpProblem = followUpProblems.get(0);
                    softly.assertThat(actualFollowUpProblem.getContent()).isEqualTo(followUpCommand.problem());
                    softly.assertThat(actualFollowUpProblem.getDifficulty()).isEqualTo(followUpDifficulty);
                }
        );
    }

    @DisplayName("[update: 루트 문제 존재, 꼬리 문제 존재하지 않는 사항] 꼬리 문제가 추가된 경우 새로 생성한다.")
    @Test
    void testUpdate2() {
        //given
        Member writer = testFixtureFactory.make(MemberBuilder.builder().build());
        Category category = testFixtureFactory.make(CategoryBuilder.builder().build());
        Problem problem = testFixtureFactory.make(
                ProblemBuilder.builder().withCategoryId(category.getId()).withWriterId(writer.getId()).withDifficulty(Difficulty.HARD).build());
        testFixtureFactory.make(ProblemScoringInfoBuilder.builder().withProblemId(problem.getId()).build());


        Difficulty followUpDifficulty = Difficulty.MEDIUM;
        ProblemUpdateCommand followUpCommand = new ProblemUpdateCommand(
                null,
                "꼬리 문제",
                "꼬리 문제 예상 답변",
                Keywords.from(Set.of("꼬리 문제 키워드1", "꼬리 문제 키워드2")),
                followUpDifficulty,
                category.getPath(),
                Collections.emptyList()
        );
        Difficulty problemDifficulty = Difficulty.EASY;
        ProblemUpdateCommand command = new ProblemUpdateCommand(
                problem.getId(),
                "문제",
                "문제 예상 답변",
                Keywords.from(Set.of("문제 키워드1", "문제 키워드2")),
                problemDifficulty,
                category.getPath(),
                List.of(followUpCommand)
        );

        //when
        problemProcessor.update(command, Association.from(writer.getId()));
        Problem actualProblem = problemReader.get(problem.getId());
        List<Problem> followUpProblems = problemReader.getFollowUpProblems(Association.from(actualProblem.getId()));

        //then
        SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(actualProblem.getContent()).isEqualTo(command.problem());
                    softly.assertThat(actualProblem.getDifficulty()).isEqualTo(problemDifficulty);
                    softly.assertThat(followUpProblems).hasSize(1);
                    Problem actualFollowUpProblem = followUpProblems.get(0);
                    softly.assertThat(actualFollowUpProblem.getContent()).isEqualTo(followUpCommand.problem());
                    softly.assertThat(actualFollowUpProblem.getDifficulty()).isEqualTo(followUpDifficulty);
                }
        );
    }

    @DisplayName("[update: 루트 문제, 꼬리 문제 모두 존재하지 않는 사항] 예외가 발생한다.")
    @Test
    void testUpdate3() {
        //given
        Member writer = testFixtureFactory.make(MemberBuilder.builder().build());
        Category category = testFixtureFactory.make(CategoryBuilder.builder().build());

        Difficulty followUpDifficulty = Difficulty.MEDIUM;
        ProblemUpdateCommand followUpCommand = new ProblemUpdateCommand(
                null,
                "꼬리 문제",
                "꼬리 문제 예상 답변",
                Keywords.from(Set.of("꼬리 문제 키워드1", "꼬리 문제 키워드2")),
                followUpDifficulty,
                category.getPath(),
                Collections.emptyList()
        );
        Difficulty problemDifficulty = Difficulty.EASY;
        ProblemUpdateCommand command = new ProblemUpdateCommand(
                null,
                "문제",
                "문제 예상 답변",
                Keywords.from(Set.of("문제 키워드1", "문제 키워드2")),
                problemDifficulty,
                category.getPath(),
                List.of(followUpCommand)
        );

        //when, then
        Assertions.assertThatThrownBy(() -> problemProcessor.update(command, Association.from(writer.getId())))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
