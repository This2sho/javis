package com.javis.learn_hub.interview.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorSortDirection;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.builder.QuestionBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterviewReaderTest {

    private TestFixtureFactory fixtureFactory = new TestFixtureFactory();
    private InterviewReader interviewReader = new InterviewReader(
            fixtureFactory.getInterviewRepository(),
            fixtureFactory.getQuestionRepository()
    );

    @DisplayName("인터뷰 id로 응답한 모든 문제 id를 가져온다.")
    @Test
    void testGetAllAnsweredProblemIds() {
        //given
        Problem problem1 = fixtureFactory.make(ProblemBuilder.builder().build());
        Problem problem2 = fixtureFactory.make(ProblemBuilder.builder().build());
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question question1 = fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.COMPLETED).withProblemId(problem1.getId())
                        .withInterviewId(interview.getId()).build());
        Question question2 = fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.UNANSWERED).withProblemId(problem2.getId())
                        .withInterviewId(interview.getId()).build());
        List<Association<Problem>> expected = List.of(question1.getProblemId());

        //when
        List<Association<Problem>> actual = interviewReader.getAllAnsweredProblemIds(
                Association.from(interview.getId()));

        //then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @DisplayName("회원 아이디로 자신의 종료된 인터뷰들을 가져온다.")
    @Test
    void testGetAllInterviews() {
        //given
        Member member = fixtureFactory.make(MemberBuilder.builder().build());
        Interview finishedInterview1 = fixtureFactory.make(InterviewBuilder.builder().withMemberId(member.getId()).build());
        finishedInterview1.finish();
        Interview finishedInterview2 = fixtureFactory.make(InterviewBuilder.builder().withMemberId(member.getId()).build());
        finishedInterview2.finish();
        Interview unFinished = fixtureFactory.make(InterviewBuilder.builder().withMemberId(member.getId()).build());

        CursorPageRequest pageRequest = CursorPageRequest.builder().withSort(CursorSortDirection.ASC).build();

        List<Interview> expected = List.of(finishedInterview1, finishedInterview2);

        //when
        List<Interview> actual = interviewReader.getAllInterviews(member.getId(), pageRequest);

        //then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual).containsExactlyElementsOf(expected);
            softly.assertThat(actual).doesNotContain(unFinished);
        });
    }

    @DisplayName("[인터뷰 재접속 상황] 인터뷰에서 답변이 안된 질문 중 꼬리 질문이 있으면 꼬리 질문을 우선 가져온다.")
    @Test
    void testGetCurrentQuestion() {
        //given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.COMPLETED).withInterviewId(interview.getId())
                        .build());
        fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.UNANSWERED)
                        .withInterviewId(interview.getId()).buildRoot());
        Question rootQuestion = fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.UNANSWERED)
                        .withInterviewId(interview.getId()).buildRoot());
        Question expected = fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.UNANSWERED)
                        .withParentQuestionId(rootQuestion.getId()).withInterviewId(interview.getId()).buildFollowUp());

        //when
        Question actual = interviewReader.getCurrentQuestion(interview);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("[인터뷰 재접속 상황] 인터뷰에서 답변이 안된 질문이 모두 루트 질문이라면 순서에 따라 가져온다.")
    @Test
    void testGetCurrentQuestion2() {
        //given
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().withTotalQuestions(3).build());
        interview.moveNextQuestion();
        fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.COMPLETED)
                        .withInterviewId(interview.getId()).withQuestionOrder(0).buildRoot());
        Question expected = fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.UNANSWERED)
                        .withInterviewId(interview.getId()).withQuestionOrder(1).buildRoot());
        Question rootQuestion2 = fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.UNANSWERED)
                        .withInterviewId(interview.getId()).withQuestionOrder(2).buildRoot());

        //when
        Question actual = interviewReader.getCurrentQuestion(interview);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("인터뷰에서 미답변 질문이 없을 경우 예외를 발생시킨다.")
    @Test
    void testGetCurrentQuestion3() {
        //given
        Problem problem1 = fixtureFactory.make(ProblemBuilder.builder().build());
        Problem problem2 = fixtureFactory.make(ProblemBuilder.builder().build());
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.COMPLETED).withProblemId(problem1.getId())
                        .withInterviewId(interview.getId()).build());
        fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.COMPLETED).withProblemId(problem1.getId())
                        .withInterviewId(interview.getId()).build());
        fixtureFactory.make(
                QuestionBuilder.builder().withQuestionStatus(QuestionStatus.COMPLETED).withProblemId(problem1.getId())
                        .withInterviewId(interview.getId()).build());

        //when, then
        assertThatThrownBy(() -> interviewReader.getCurrentQuestion(interview));
    }
}
