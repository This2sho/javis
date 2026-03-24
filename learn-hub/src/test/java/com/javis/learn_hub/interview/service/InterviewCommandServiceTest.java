package com.javis.learn_hub.interview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.evaluation.domain.service.EvaluationReader;
import com.javis.learn_hub.event.EvaluationRetryEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.interview.domain.service.InterviewFinder;
import com.javis.learn_hub.interview.domain.service.InterviewProcessor;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.interview.service.dto.QuestionResponse;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.score.service.ScoreService;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.builder.QuestionBuilder;
import com.javis.learn_hub.support.config.WithMockEventPublisher;
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
class InterviewCommandServiceTest {

    private final TestFixtureFactory fixtureFactory = new TestFixtureFactory();

    @MockitoBean
    private InterviewProcessor interviewProcessor;

    @MockitoBean
    private InterviewReader interviewReader;

    @MockitoBean
    private InterviewFinder interviewFinder;

    @MockitoBean
    private AnswerReader answerReader;

    @MockitoBean
    private EvaluationReader evaluationReader;

    @MockitoBean
    private NextQuestionService nextQuestionService;

    @MockitoBean
    private ScoreService scoreService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private InterviewCommandService interviewCommandService;

    @BeforeEach
    void resetMocks() {
        reset(applicationEventPublisher);
    }

    @DisplayName("문제 추천으로 시작 질문들을 선별하고 인터뷰를 시작한다.")
    @Test
    void testStart() {
        //given
        given(interviewFinder.findActiveInterview(MainCategory.COMPUTER_SCIENCE, 1L)).willReturn(Optional.empty());
        List<Question> rootQuestions = List.of(fixtureFactory.make(QuestionBuilder.builder().build()), fixtureFactory.make(QuestionBuilder.builder().build()),
                fixtureFactory.make(QuestionBuilder.builder().build()));
        Question firstQuestion = rootQuestions.get(0);
        given(interviewProcessor.initInterview(any(), any()))
                .willReturn(rootQuestions);
        QuestionResponse expected = QuestionResponse.from(firstQuestion);

        //when
        QuestionResponse actual = interviewCommandService.start(MainCategory.COMPUTER_SCIENCE.name(), 1L);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("채점 요청 후 서버가 중단된 상태로 재접속하면 재채점 이벤트를 발행하고 pending 응답을 반환한다.")
    @Test
    void testStartWhenAnsweredQuestionNeedsEvaluation() {
        // given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question answeredQuestion = fixtureFactory.make(
                QuestionBuilder.builder()
                        .withInterviewId(interview.getId())
                        .withQuestionStatus(QuestionStatus.ANSWERED)
                        .buildRoot()
        );
        Answer failedAnswer = mock(Answer.class);
        given(failedAnswer.needsEvaluation()).willReturn(true);

        given(interviewFinder.findActiveInterview(MainCategory.COMPUTER_SCIENCE, 1L)).willReturn(Optional.of(interview));
        given(interviewReader.getCurrentQuestion(interview)).willReturn(answeredQuestion);
        given(answerReader.getByQuestionId(answeredQuestion.getId())).willReturn(failedAnswer);

        // when
        QuestionResponse actual = interviewCommandService.start(MainCategory.COMPUTER_SCIENCE.name(), 1L);

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
                        .withQuestionStatus(QuestionStatus.ANSWERED)
                        .buildRoot()
        );
        Answer scoredAnswer = mock(Answer.class);
        Evaluation evaluation = mock(Evaluation.class);
        given(scoredAnswer.needsEvaluation()).willReturn(false);
        given(scoredAnswer.getId()).willReturn(10L);
        given(evaluation.getPreferences()).willReturn(List.of(Difficulty.MEDIUM, Difficulty.HARD, Difficulty.EASY));

        given(interviewFinder.findActiveInterview(MainCategory.COMPUTER_SCIENCE, 1L)).willReturn(Optional.of(interview));
        given(interviewReader.getCurrentQuestion(interview)).willReturn(answeredQuestion);
        given(answerReader.getByQuestionId(answeredQuestion.getId())).willReturn(scoredAnswer);
        given(evaluationReader.getByAnswerId(scoredAnswer.getId())).willReturn(evaluation);

        // when
        QuestionResponse actual = interviewCommandService.start(MainCategory.COMPUTER_SCIENCE.name(), 1L);

        // then
        assertThat(actual).isEqualTo(QuestionResponse.waitingForNextQuestion(answeredQuestion));
        verify(nextQuestionService).continueNextQuestion(eq(answeredQuestion.getId()), any());
    }

    @DisplayName("[꼬리 질문이 있는 경우] 다음 문제로 꼬리 질문으로 인터뷰를 진행한다.")
    @Test
    void testContinueNextQuestion() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Long questionId = 1L;

        //when
        interviewCommandService.continueNextQuestion(questionId, preferences);

        //then
        verify(nextQuestionService).continueNextQuestion(questionId, preferences);
    }

    @DisplayName("[꼬리 질문이 없는 경우] 다음 시작 질문으로 인터뷰를 진행한다.")
    @Test
    void testContinueNextQuestion2() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Long questionId = 2L;

        //when
        interviewCommandService.continueNextQuestion(questionId, preferences);

        //then
        verify(nextQuestionService).continueNextQuestion(questionId, preferences);
    }

    @DisplayName("다음 질문 진행 요청은 NextQuestionService에 위임한다.")
    @Test
    void testContinueNextQuestion3() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Long questionId = 3L;

        //when
        interviewCommandService.continueNextQuestion(questionId, preferences);

        //then
        verify(nextQuestionService).continueNextQuestion(questionId, preferences);
    }

}
