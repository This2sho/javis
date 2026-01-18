package com.javis.learn_hub.interview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.event.InterviewFinishEvent;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WithMockEventPublisher
@SpringBootTest
class InterviewCommandServiceTest {

    private TestFixtureFactory fixtureFactory = new TestFixtureFactory();

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
        Long memberId = 1L;
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question previousQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId()).build());
        Question followUpQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId())
                .withParentQuestionId(previousQuestion.getId()).build());
        given(interviewReader.getQuestion(previousQuestion.getId())).willReturn(previousQuestion);
        given(interviewReader.get(previousQuestion.getInterviewId())).willReturn(interview);
        given(interviewProcessor.proceedToFollowUpQuestion(previousQuestion, preferences))
                .willReturn(Optional.of(followUpQuestion));

        //when
        InterviewerResponse actual = interviewCommandService.continueNextQuestion(previousQuestion.getId(),
                preferences);

        //then
        assertSoftly(softly -> {
            assertThat(actual.ended()).isFalse();
            assertThat(actual.interviewerMessage()).isEqualTo(followUpQuestion.getMessage());
        });
    }

    @DisplayName("[꼬리 질문이 없는 경우] 다음 시작 질문으로 인터뷰를 진행한다.")
    @Test
    void testContinueNextQuestion2() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Long memberId = 1L;
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
        InterviewerResponse actual = interviewCommandService.continueNextQuestion(previousQuestion.getId(),
                preferences);

        //then
        assertSoftly(softly -> {
            assertThat(actual.ended()).isFalse();
            assertThat(actual.interviewerMessage()).isEqualTo(nextRootQuestion.getMessage());
        });
    }

    @DisplayName("[인터뷰가 종료된 경우] 인터뷰 종료 응답을 반환하고 인터뷰 종료 이벤트를 발행한다.")
    @Test
    void testContinueNextQuestion3() {
        //given
        List<Difficulty> preferences = List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        Long memberId = 1L;
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        interview.finish();
        Question previousQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId()).build());

        given(interviewReader.getQuestion(previousQuestion.getId())).willReturn(previousQuestion);
        given(interviewReader.get(previousQuestion.getInterviewId())).willReturn(interview);
        given(interviewProcessor.finish(eq(interview))).willReturn(List.of(new InterviewFinishEvent(interview.getId(), interview.getMemberId().getId())));
        //when
        InterviewerResponse actual = interviewCommandService.continueNextQuestion(previousQuestion.getId(),
                preferences);

        //then
        assertSoftly(softly -> {
            assertThat(actual.ended()).isTrue();
            assertThat(actual).isEqualTo(InterviewerResponse.finished(interview.getId()));
            verify(applicationEventPublisher).publishEvent(new InterviewFinishEvent(interview.getId(), memberId));
        });
    }

    @DisplayName("[질문에 답변이 완료 된 상황] 다음 질문으로 넘어가기 위해 답변 완료 처리를 한다.")
    @Test
    void testCompleteCurrentQuestion() {
        //given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question currentQuestion = fixtureFactory.make(QuestionBuilder.builder().withInterviewId(interview.getId()).build());
        given(interviewReader.getQuestion(currentQuestion.getId())).willReturn(currentQuestion);
        given(interviewReader.get(currentQuestion.getInterviewId())).willReturn(interview);

        //when
        interviewCommandService.completeCurrentQuestion(currentQuestion.getId());

        //then
        verify(interviewProcessor).finalizeCurrentQuestion(currentQuestion);
    }
}
