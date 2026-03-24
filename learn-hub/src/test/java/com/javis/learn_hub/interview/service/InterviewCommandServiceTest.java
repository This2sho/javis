package com.javis.learn_hub.interview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.event.InterviewFinishEvent;
import com.javis.learn_hub.event.NextQuestionReadyEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewProcessor;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.interview.service.dto.InterviewerResponse;
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

    @DisplayName("[꼬리 질문이 있는 경우] 다음 문제로 꼬리 질문으로 인터뷰를 진행한다.")
    @Test
    void testContinueNextQuestion() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question previousQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId()).build());
        Question followUpQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId())
                .withParentQuestionId(previousQuestion.getId()).build());
        given(interviewReader.getQuestion(previousQuestion.getId())).willReturn(previousQuestion);
        given(interviewReader.get(previousQuestion.getInterviewId())).willReturn(interview);
        given(interviewProcessor.proceedToFollowUpQuestion(previousQuestion, preferences))
                .willReturn(Optional.of(followUpQuestion));

        //when
        interviewCommandService.continueNextQuestion(previousQuestion.getId(), preferences);

        //then
        verify(applicationEventPublisher).publishEvent(
                new NextQuestionReadyEvent(
                        interview.getMemberId().getId(),
                        new InterviewerResponse(false, interview.getId(), followUpQuestion.getId(), followUpQuestion.getMessage())
                )
        );
    }

    @DisplayName("[꼬리 질문이 없는 경우] 다음 시작 질문으로 인터뷰를 진행한다.")
    @Test
    void testContinueNextQuestion2() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question previousQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId()).build());
        Question nextRootQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId())
                .withParentQuestionId(previousQuestion.getId()).build());

        given(interviewReader.getQuestion(previousQuestion.getId())).willReturn(previousQuestion);
        given(interviewReader.get(previousQuestion.getInterviewId())).willReturn(interview);
        given(interviewProcessor.proceedToFollowUpQuestion(previousQuestion, preferences))
                .willReturn(Optional.empty());
        given(interviewProcessor.proceedToNextRootQuestion(interview))
                .willReturn(Optional.of(nextRootQuestion));

        //when
        interviewCommandService.continueNextQuestion(previousQuestion.getId(), preferences);

        //then
        verify(applicationEventPublisher).publishEvent(
                new NextQuestionReadyEvent(
                        interview.getMemberId().getId(),
                        new InterviewerResponse(false, interview.getId(), nextRootQuestion.getId(), nextRootQuestion.getMessage())
                )
        );
    }

    @DisplayName("[인터뷰가 종료된 경우] 인터뷰 종료 이벤트와 NextQuestionReadyEvent를 발행한다.")
    @Test
    void testContinueNextQuestion3() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Long memberId = interview.getMemberId().getId();
        Question previousQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId()).build());
        InterviewFinishEvent finishEvent = new InterviewFinishEvent(interview.getId(), memberId);

        given(interviewReader.getQuestion(previousQuestion.getId())).willReturn(previousQuestion);
        given(interviewReader.get(previousQuestion.getInterviewId())).willReturn(interview);
        given(interviewProcessor.proceedToFollowUpQuestion(previousQuestion, preferences)).willReturn(Optional.empty());
        given(interviewProcessor.proceedToNextRootQuestion(interview)).willReturn(Optional.empty());
        given(interviewProcessor.finish(interview)).willReturn(finishEvent);

        //when
        interviewCommandService.continueNextQuestion(previousQuestion.getId(), preferences);

        //then
        verify(applicationEventPublisher).publishEvent(finishEvent);
        verify(applicationEventPublisher).publishEvent(
                new NextQuestionReadyEvent(memberId, InterviewerResponse.finished(interview.getId()))
        );
    }

}
