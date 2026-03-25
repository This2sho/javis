package com.javis.learn_hub.interview.domain.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;

import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.dto.NextQuestionResult;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.problem.domain.service.ProblemRecommender;
import com.javis.learn_hub.score.domain.service.CategoryRecommender;
import com.javis.learn_hub.score.domain.service.ScoreReader;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.builder.QuestionBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuestionFlowProcessorTest {

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
    private final ProblemReader problemReader = new ProblemReader(
            fixtureFactory.getProblemRepository(),
            fixtureFactory.getProblemScoringInfoRepository()
    );
    private final QuestionFlowProcessor questionFlowProcessor = new QuestionFlowProcessor(
            fixtureFactory.getQuestionRepository(),
            interviewReader,
            new ProblemRecommender(categoryRecommender, problemReader, fixtureFactory.getProblemRepository())
    );

    @DisplayName("[인터뷰이가 대답 후 다음 질문 고르는 상황] 이전 질문과 난이도 선호도로 꼬리 질문을 생성한다.")
    @Test
    void testProceedToFollowUpQuestion() {
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());

        Problem firstProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Question previousQuestion = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withProblemId(firstProblem.getId()).build()
        );
        Problem relatedProblem = fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(firstProblem.getId()).build()
        );

        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        NextQuestionResult result = questionFlowProcessor.continueNextQuestion(previousQuestion.getId(), preferences);
        Question nextQuestion = interviewReader.getAllQuestions(previousQuestion.getInterviewId()).stream()
                .filter(Question::isFollowUpQuestion)
                .findFirst()
                .orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(result.hasNextQuestion()).isTrue();
            softly.assertThat(nextQuestion.getProblemId()).isEqualTo(Association.from(relatedProblem.getId()));
        });
    }

    @DisplayName("[다음 루트 질문이 존재하는 경우] 다음 시작 질문으로 진행한다.")
    @Test
    void testProceedToFollowUpQuestion2() {
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().withTotalQuestions(2).build());

        Problem firstProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Question previousQuestion = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withProblemId(firstProblem.getId()).withQuestionOrder(0).buildRoot()
        );
        Question nextRootQuestion = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withQuestionOrder(1).buildRoot()
        );

        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        NextQuestionResult result = questionFlowProcessor.continueNextQuestion(previousQuestion.getId(), preferences);

        assertSoftly(softly -> {
            softly.assertThat(result.hasNextQuestion()).isTrue();
            softly.assertThat(result.nextQuestion().orElseThrow()).isEqualTo(nextRootQuestion);
        });
    }

    @DisplayName("[추천 꼬리 문제와 다음 루트 질문이 모두 없는 경우] 인터뷰를 종료한다.")
    @Test
    void testContinueNextQuestionWhenNoMoreQuestions() {
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().withTotalQuestions(1).build());
        Problem firstProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Question previousQuestion = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withProblemId(firstProblem.getId()).buildRoot()
        );

        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        NextQuestionResult result = questionFlowProcessor.continueNextQuestion(previousQuestion.getId(), preferences);

        assertSoftly(softly -> {
            softly.assertThat(result.hasNextQuestion()).isFalse();
            softly.assertThat(interview.isFinished()).isFalse();
        });
    }

    @DisplayName("[꼬리 질문 depth가 최대치인 경우] 더 이상 꼬리 질문을 만들지 않고 다음 루트 질문으로 진행한다.")
    @Test
    void testContinueNextQuestionWhenFollowUpDepthReachedLimit() {
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().withTotalQuestions(2).build());

        Problem firstProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Question previousQuestion = fixtureFactory.make(
                QuestionBuilder.builder()
                        .withInterviewId(interview.getId())
                        .withProblemId(firstProblem.getId())
                        .withParentQuestionId(999L)
                        .withDepth(QuestionFlowProcessor.MAX_DEPTH)
                        .buildFollowUp()
        );
        fixtureFactory.make(
                ProblemBuilder.builder().withParentProblemId(firstProblem.getId()).build()
        );
        Question nextRootQuestion = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withQuestionOrder(1).buildRoot()
        );

        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        NextQuestionResult result = questionFlowProcessor.continueNextQuestion(previousQuestion.getId(), preferences);

        assertSoftly(softly -> {
            softly.assertThat(result.hasNextQuestion()).isTrue();
            softly.assertThat(result.nextQuestion().orElseThrow()).isEqualTo(nextRootQuestion);
        });
        assertThat(interviewReader.getAllQuestions(Association.from(interview.getId())).stream()
                .filter(Question::isFollowUpQuestion)
                .count()).isEqualTo(1);
    }
}
