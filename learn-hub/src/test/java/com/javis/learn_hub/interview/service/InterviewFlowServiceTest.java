package com.javis.learn_hub.interview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.event.EvaluationRetryEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewProcessor;
import com.javis.learn_hub.interview.domain.service.InterviewStepFinder;
import com.javis.learn_hub.interview.domain.service.QuestionFlowProcessor;
import com.javis.learn_hub.interview.domain.service.dto.InterviewStepResult;
import com.javis.learn_hub.interview.domain.service.dto.NextQuestionResult;
import com.javis.learn_hub.interview.service.dto.InterviewerResponse;
import com.javis.learn_hub.interview.service.dto.QuestionResponse;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.score.service.ScoreService;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.builder.QuestionBuilder;
import com.javis.learn_hub.support.config.WithMockEventPublisher;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WithMockEventPublisher
@SpringBootTest
class InterviewFlowServiceTest {

    private final TestFixtureFactory fixtureFactory = new TestFixtureFactory();

    @MockitoBean
    private InterviewProcessor interviewProcessor;

    @MockitoBean
    private InterviewStepFinder interviewStepFinder;

    @MockitoBean
    private QuestionFlowProcessor questionFlowProcessor;

    @MockitoBean
    private ScoreService scoreService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private InterviewFlowService interviewFlowService;

    @BeforeEach
    void resetMocks() {
        reset(applicationEventPublisher);
    }

    @DisplayName("문제 추천으로 시작 질문들을 선별하고 인터뷰를 시작한다.")
    @Test
    void testStart() {
        //given
        given(interviewStepFinder.findActiveInterview(MainCategory.COMPUTER_SCIENCE, 1L, ContentLanguage.KO))
                .willReturn(Optional.empty());
        List<Question> rootQuestions = List.of(fixtureFactory.make(QuestionBuilder.builder().build()), fixtureFactory.make(QuestionBuilder.builder().build()),
                fixtureFactory.make(QuestionBuilder.builder().build()));
        Question firstQuestion = rootQuestions.get(0);
        given(interviewProcessor.initInterview(MainCategory.COMPUTER_SCIENCE, 1L, ContentLanguage.KO))
                .willReturn(rootQuestions);
        QuestionResponse expected = QuestionResponse.from(firstQuestion);

        //when
        QuestionResponse actual = interviewFlowService.start(MainCategory.COMPUTER_SCIENCE.name(), 1L);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("채점 요청 후 서버가 중단된 상태로 재접속하면 재채점 이벤트를 발행하고 pending 응답을 반환한다.")
    @Test
    void testStartWhenAnsweredQuestionNeedsEvaluation() {
        // given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question answeredQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId()).buildRoot());
        InterviewStepResult step = InterviewStepResult.pendingEvaluation(answeredQuestion);

        given(interviewStepFinder.findActiveInterview(MainCategory.COMPUTER_SCIENCE, 1L, ContentLanguage.KO))
                .willReturn(Optional.of(interview));
        given(interviewStepFinder.find(interview)).willReturn(step);

        // when
        QuestionResponse actual = interviewFlowService.start(MainCategory.COMPUTER_SCIENCE.name(), 1L);

        // then
        assertThat(actual).isEqualTo(QuestionResponse.pendingEvaluation(answeredQuestion));
        verify(applicationEventPublisher).publishEvent(new EvaluationRetryEvent(answeredQuestion.getId()));
    }

    @DisplayName("채점은 완료됐지만 다음 질문 생성 전에 서버가 중단되면 재접속 시 다음 질문 진행을 재개한다.")
    @Test
    void testStartWhenAnsweredQuestionAlreadyScored() {
        // given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question answeredQuestion = fixtureFactory.make(
                QuestionBuilder.builder()
                        .withInterviewId(interview.getId())
                        .buildRoot()
        );
        List<Difficulty> preferences = List.of(Difficulty.MEDIUM, Difficulty.HARD, Difficulty.EASY);
        InterviewStepResult step = InterviewStepResult.waitingForNextQuestion(answeredQuestion, preferences);
        Question nextQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId()).buildRoot());
        NextQuestionResult nextQuestionResult = NextQuestionResult.withNextQuestion(interview, nextQuestion);

        given(interviewStepFinder.findActiveInterview(MainCategory.COMPUTER_SCIENCE, 1L, ContentLanguage.KO))
                .willReturn(Optional.of(interview));
        given(interviewStepFinder.find(interview)).willReturn(step);
        given(questionFlowProcessor.continueNextQuestion(answeredQuestion.getId(), preferences)).willReturn(nextQuestionResult);

        // when
        QuestionResponse actual = interviewFlowService.start(MainCategory.COMPUTER_SCIENCE.name(), 1L);

        // then
        assertThat(actual).isEqualTo(QuestionResponse.waitingForNextQuestion(answeredQuestion));
        verify(questionFlowProcessor).continueNextQuestion(answeredQuestion.getId(), preferences);
    }

    @DisplayName("[꼬리 질문이 있는 경우] 다음 문제 진행 요청은 QuestionFlowProcessor에 위임하고 이벤트를 발행한다.")
    @Test
    void testContinueNextQuestion() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Long questionId = 1L;
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question nextQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId()).buildRoot());
        NextQuestionResult result = NextQuestionResult.withNextQuestion(interview, nextQuestion);
        given(questionFlowProcessor.continueNextQuestion(questionId, preferences)).willReturn(result);

        //when
        interviewFlowService.continueNextQuestion(questionId, preferences);

        //then
        verify(questionFlowProcessor).continueNextQuestion(questionId, preferences);
        verify(applicationEventPublisher).publishEvent(
                new com.javis.learn_hub.event.NextQuestionReadyEvent(
                        interview.getMemberId().getId(),
                        InterviewerResponse.nextQuestion(interview.getId(), nextQuestion.getId(), nextQuestion.getMessage())
                )
        );
    }

    @DisplayName("[꼬리 질문이 없는 경우] 다음 시작 질문으로 인터뷰를 진행한다.")
    @Test
    void testContinueNextQuestion2() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Long questionId = 2L;
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        NextQuestionResult result = NextQuestionResult.finished(interview);
        given(questionFlowProcessor.continueNextQuestion(questionId, preferences)).willReturn(result);
        given(interviewProcessor.finish(interview)).willReturn(new com.javis.learn_hub.event.InterviewFinishEvent(interview.getId(), interview.getMemberId().getId()));

        //when
        interviewFlowService.continueNextQuestion(questionId, preferences);

        //then
        verify(questionFlowProcessor).continueNextQuestion(questionId, preferences);
        verify(interviewProcessor).finish(interview);
    }

    @DisplayName("답변 생성 이벤트 후 질문을 answered 상태로 변경한다.")
    @Test
    void testMarkQuestionAnswered() {
        //given
        Long questionId = 3L;

        //when
        interviewFlowService.markQuestionAnswered(questionId);

        //then
        verify(questionFlowProcessor).markQuestionAnswered(questionId);
    }

    @DisplayName("같은 카테고리라도 언어가 다르면 활성 인터뷰를 재사용하지 않고 새 인터뷰를 시작한다.")
    @Test
    void testStartWithDifferentLanguageStartsNewInterview() {
        given(interviewStepFinder.findActiveInterview(MainCategory.COMPUTER_SCIENCE, 1L, ContentLanguage.EN))
                .willReturn(Optional.empty());
        List<Question> rootQuestions = List.of(
                fixtureFactory.make(QuestionBuilder.builder().withContentLanguage(ContentLanguage.EN).build())
        );
        given(interviewProcessor.initInterview(MainCategory.COMPUTER_SCIENCE, 1L, ContentLanguage.EN))
                .willReturn(rootQuestions);

        QuestionResponse actual = interviewFlowService.start(
                MainCategory.COMPUTER_SCIENCE.name(),
                1L,
                ContentLanguage.EN
        );

        assertThat(actual).isEqualTo(QuestionResponse.from(rootQuestions.get(0)));
        verify(interviewProcessor).initInterview(MainCategory.COMPUTER_SCIENCE, 1L, ContentLanguage.EN);
    }

}
