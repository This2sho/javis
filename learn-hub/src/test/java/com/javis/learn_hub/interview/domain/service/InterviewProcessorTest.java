package com.javis.learn_hub.interview.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.event.InterviewFinishEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.problem.domain.service.ProblemRecommender;
import com.javis.learn_hub.score.domain.service.CategoryRecommender;
import com.javis.learn_hub.score.domain.service.ScoreReader;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.CategoryBuilder;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.builder.QuestionBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterviewProcessorTest {

    private final TestFixtureFactory fixtureFactory = new TestFixtureFactory();
    private final InterviewReader interviewReader = new InterviewReader(fixtureFactory.getInterviewRepository(),
            fixtureFactory.getQuestionRepository());

    private final CategoryReader categoryReader = new CategoryReader(fixtureFactory.getCategoryRepository());
    private final CategoryRecommender categoryRecommender = new CategoryRecommender(
            new ScoreReader(fixtureFactory.getScoreRepository(), categoryReader),
            categoryReader
    );

    private final ProblemReader problemReader = new ProblemReader(fixtureFactory.getProblemRepository(), fixtureFactory.getProblemScoringInfoRepository());

    private final InterviewProcessor interviewProcessor = new InterviewProcessor(
            fixtureFactory.getInterviewRepository(),
            fixtureFactory.getQuestionRepository(),
            interviewReader,
            new ProblemRecommender(categoryRecommender, problemReader, fixtureFactory.getProblemRepository())
    );

    @DisplayName("[인터뷰 시작 상황] 메인 카테고리로 5개의 문제를 추천 받아 질문을 생성한다.")
    @Test
    void testInitInterview() {
        //given
        MainCategory mainCategory = MainCategory.COMPUTER_SCIENCE;
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

    @DisplayName("[인터뷰이가 대답 후 다음 질문 고르는 상황] 이전 질문과 난이도 선호도로 꼬리 질문을 생성한다.")
    @Test
    void testProceedToFollowUpQuestion() {
        //given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());

        Problem firstProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Question previousQuestion = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withProblemId(firstProblem.getId())
                        .build());

        Problem realatedProblem = fixtureFactory.make(ProblemBuilder.builder().withParentProblemId(firstProblem.getId()).build());

        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        //when
        Optional<Question> nextQuestion = interviewProcessor.proceedToFollowUpQuestion(previousQuestion, preferences);

        //then
        assertSoftly(softly -> {
            assertThat(nextQuestion).isPresent();
            assertThat(nextQuestion.get().getProblemId()).isEqualTo(Association.from(realatedProblem.getId()));
        });
    }

    @DisplayName("[추천 꼬리 문제가 없는 경우] Optional.empty()를 반환한다.")
    @Test
    void testProceedToFollowUpQuestion2() {
        //given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());

        Problem firstProblem = fixtureFactory.make(ProblemBuilder.builder().build());
        Question previousQuestion = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withProblemId(firstProblem.getId())
                        .build());

        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);

        //when
        Optional<Question> nextQuestion = interviewProcessor.proceedToFollowUpQuestion(previousQuestion, preferences);

        //then
        assertThat(nextQuestion).isEmpty();
    }

    @DisplayName("[다음 시작 질문이 존재하는 경우] 다음 시작 질문을 반환한다.")
    @Test
    void testProceedToNextRootQuestion() {
        //given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().withTotalQuestions(2).build());
        Question rootQuestion1 = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withQuestionOrder(0).build());
        Question rootQuestion2 = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withQuestionOrder(1).build());

        //when
        Optional<Question> nextRootQuestion = interviewProcessor.proceedToNextRootQuestion(interview);

        //then
        assertSoftly(softly -> {
            assertThat(nextRootQuestion).isPresent();
            assertThat(nextRootQuestion.get()).isEqualTo(rootQuestion2);
        });
    }

    @DisplayName("[다음 시작 질문이 존재하지 않는 경우] Optional.empty()를 반환한다.")
    @Test
    void testProceedToNextRootQuestion2() {
        //given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().withTotalQuestions(2).build());
        Question rootQuestion1 = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withQuestionOrder(0).build());
        Question rootQuestion2 = fixtureFactory.make(
                QuestionBuilder.builder().withInterviewId(interview.getId()).withQuestionOrder(1).build());
        interview.moveNextQuestion();

        //when
        Optional<Question> nextRootQuestion = interviewProcessor.proceedToNextRootQuestion(interview);

        //then
        assertThat(nextRootQuestion).isEmpty();
    }

    @DisplayName("[채점 완료 된 상황] 질문을 완료 상태로 변경한다.")
    @Test
    void testMarkQuestionCompleted() {
        //given
        Question question = fixtureFactory.make(QuestionBuilder.builder().build());

        //when
        interviewProcessor.markQuestionCompleted(question);

        //then
        assertThat(question.getQuestionStatus()).isEqualTo(QuestionStatus.COMPLETED);
    }

    @DisplayName("[모든 질문이 완료 된 상황] 인터뷰 종료시 인터뷰 종료이벤트를 발행한다.")
    @Test
    void testFinish() {
        //given
        Member member = fixtureFactory.make(MemberBuilder.builder().build());
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().withMemberId(member.getId()).build());
        InterviewFinishEvent interviewFinishEvent = new InterviewFinishEvent(interview.getId(),
                interview.getMemberId().getId());

        //when
        List<DomainEvent> events = interviewProcessor.finish(interview);

        //then
        assertSoftly(softly -> {
            softly.assertThat(interview.isFinished()).isTrue();
            softly.assertThat(events).contains(interviewFinishEvent);
        });
    }
}
