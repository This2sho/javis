package com.javis.learn_hub.interview.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.problem.domain.service.ProblemRecommender;
import com.javis.learn_hub.score.domain.service.CategoryRecommender;
import com.javis.learn_hub.score.domain.service.ScoreReader;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.CategoryBuilder;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterviewProcessorTest {

    private final TestFixtureFactory fixtureFactory = new TestFixtureFactory();
    private final AnswerReader answerReader = new AnswerReader(fixtureFactory.getAnswerRepository());
    private final InterviewReader interviewReader = new InterviewReader(
            fixtureFactory.getInterviewRepository(),
            fixtureFactory.getQuestionRepository(),
            answerReader
    );

    private final CategoryReader categoryReader = new CategoryReader(fixtureFactory.getCategoryRepository());
    private final CategoryRecommender categoryRecommender = new CategoryRecommender(
            new ScoreReader(fixtureFactory.getScoreRepository(), categoryReader),
            categoryReader
    );

    private final ProblemReader problemReader = new ProblemReader(fixtureFactory.getProblemRepository(), fixtureFactory.getProblemScoringInfoRepository());
    private final ProblemRecommender problemRecommender = new ProblemRecommender(
            categoryRecommender,
            problemReader,
            fixtureFactory.getProblemRepository()
    );
    private final QuestionFlowProcessor questionFlowProcessor = new QuestionFlowProcessor(
            fixtureFactory.getQuestionRepository(),
            interviewReader,
            problemRecommender
    );

    private final InterviewProcessor interviewProcessor = new InterviewProcessor(
            fixtureFactory.getInterviewRepository(),
            problemRecommender,
            questionFlowProcessor,
            new InterviewStartPolicy(fixtureFactory.getMemberRepository(), fixtureFactory.getInterviewRepository())
    );

    @DisplayName("[인터뷰 시작 상황] 메인 카테고리로 5개의 문제를 추천 받아 질문을 생성한다.")
    @Test
    void testInitInterview() {
        //given
        MainCategory mainCategory = MainCategory.COMPUTER_SCIENCE;
        fixtureFactory.make(com.javis.learn_hub.support.builder.MemberBuilder.builder().withSocialId(1L).build());
        Category category = fixtureFactory.make(CategoryBuilder.builder().withMainCategory(mainCategory).build());
        List<Problem> problems = fixtureFactory.make5ProblemsWithCategory(category);
        List<Long> expectedProblemIds = problems.stream().map(Problem::getId)
                .toList();

        //when
        List<Question> rootQuestions = interviewProcessor.initInterview(mainCategory, 1L);

        //then
        List<Long> actualProblemIds = rootQuestions.stream()
                .map(question -> question.getProblemId().getId())
                .toList();
        assertThat(actualProblemIds).containsExactlyInAnyOrderElementsOf(expectedProblemIds);
    }
}
