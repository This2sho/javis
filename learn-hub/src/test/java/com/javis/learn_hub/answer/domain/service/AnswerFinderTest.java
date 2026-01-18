package com.javis.learn_hub.answer.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.javis.learn_hub.answer.domain.EvaluationResult;
import com.javis.learn_hub.answer.domain.Grade;
import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.support.SimpleAnswerReader;
import com.javis.learn_hub.support.SimpleInterviewReader;
import com.javis.learn_hub.support.SimpleProblemReader;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.AnswerBuilder;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.builder.QuestionBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnswerFinderTest {

    private final TestFixtureFactory testFixtureFactory = new TestFixtureFactory();

    private final AnswerFinder answerFinder = new AnswerFinder(
            new SimpleAnswerReader(testFixtureFactory.getAnswerRepository()),
            new SimpleInterviewReader(testFixtureFactory.getInterviewRepository(),
                    testFixtureFactory.getQuestionRepository()),
            new SimpleProblemReader(testFixtureFactory.getProblemRepository())
    );

    @DisplayName("인터뷰 아이디로 질문 카테고리, 답변 채점 등급 한 쌍을 가져온다.")
    @Test
    void testFindCategoryGrades() {
        //given 질문, 문제, 답변 있어야함.
        Interview interview = testFixtureFactory.make(InterviewBuilder.builder().build());
        Problem problem = testFixtureFactory.make(ProblemBuilder.builder().build());
        Question question = testFixtureFactory.make(
                QuestionBuilder.builder().withProblemId(problem.getId()).withInterviewId(interview.getId()).build());
        EvaluationResult evaluationResult = new EvaluationResult(Grade.PERFECT, "완벽한 답변이었습니다.");
        testFixtureFactory.make(AnswerBuilder.builder().withQuestionId(question.getId())
                .withEvaluationResult(evaluationResult).build());

        //when
        List<CategoryGrade> actual = answerFinder.findCategoryGrades(Association.from(interview.getId()));

        //then
        SoftAssertions.assertSoftly(at -> {
            assertThat(actual).hasSize(1);
            assertThat(actual.get(0).categoryId()).isEqualTo(problem.getCategoryId().getId());
            assertThat(actual.get(0).grade()).isEqualTo(evaluationResult.getGrade());
        });
    }

    @DisplayName("인터뷰 아이디로 질문 카테고리, 답변 채점 등급 모든 쌍을 가져온다.")
    @Test
    void testFindCategoryGrades2() {
        //given
        Interview interview = testFixtureFactory.make(InterviewBuilder.builder().build());

        Problem problem1 = testFixtureFactory.make(ProblemBuilder.builder().build());
        Problem problem2 = testFixtureFactory.make(ProblemBuilder.builder().build());

        Question question1 = testFixtureFactory.make(
                QuestionBuilder.builder().withProblemId(problem1.getId()).withInterviewId(interview.getId()).build());
        Question question2 = testFixtureFactory.make(
                QuestionBuilder.builder().withProblemId(problem2.getId()).withInterviewId(interview.getId()).build());

        EvaluationResult perfect = new EvaluationResult(Grade.PERFECT, "완벽한 답변이었습니다.");
        EvaluationResult good = new EvaluationResult(Grade.GOOD, "좋은 답변이었습니다.");

        testFixtureFactory.make(AnswerBuilder.builder().withQuestionId(question1.getId())
                .withEvaluationResult(perfect).build());
        testFixtureFactory.make(AnswerBuilder.builder().withQuestionId(question2.getId())
                .withEvaluationResult(good).build());

        CategoryGrade expected1 = new CategoryGrade(problem1.getCategoryId().getId(), Grade.PERFECT);
        CategoryGrade expected2 = new CategoryGrade(problem2.getCategoryId().getId(), Grade.GOOD);

        //when
        List<CategoryGrade> actual = answerFinder.findCategoryGrades(Association.from(interview.getId()));

        //then
        SoftAssertions.assertSoftly(at -> {
            assertThat(actual).hasSize(2);
            assertThat(actual).containsAll(List.of(expected1, expected2));
        });
    }
}
