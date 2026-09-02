package com.javis.learn_hub.interview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerFinder;
import com.javis.learn_hub.answer.domain.service.dto.QnA;
import com.javis.learn_hub.evaluation.domain.analysis.EvaluationAnalysis;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.interview.service.dto.InterviewHistoryDetailResponse;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.AnswerBuilder;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.builder.QuestionBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewQueryServiceTest {

    private final TestFixtureFactory fixtureFactory = new TestFixtureFactory();

    @Mock
    private InterviewReader interviewReader;

    @Mock
    private AnswerFinder answerFinder;

    @InjectMocks
    private InterviewQueryService interviewQueryService;

    @DisplayName("인터뷰 상세는 실제 답변 순서대로 정렬하고 루트 기준 번호를 붙여 반환한다.")
    @Test
    void viewHistorySortsByActualInterviewFlowAndAddsDisplayOrder() {
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().withTotalQuestions(2).build());
        Question root1 = fixtureFactory.make(
                QuestionBuilder.builder()
                        .withInterviewId(interview.getId())
                        .withQuestionOrder(0)
                        .withQuestionStatus(QuestionStatus.ANSWERED)
                        .withMessage("root-1")
                        .buildRoot()
        );
        Question root2 = fixtureFactory.make(
                QuestionBuilder.builder()
                        .withInterviewId(interview.getId())
                        .withQuestionOrder(1)
                        .withQuestionStatus(QuestionStatus.ANSWERED)
                        .withMessage("root-2")
                        .buildRoot()
        );
        Question followUp11 = fixtureFactory.make(
                QuestionBuilder.builder()
                        .withInterviewId(interview.getId())
                        .withParentQuestionId(root1.getId())
                        .withDepth(1)
                        .withQuestionStatus(QuestionStatus.ANSWERED)
                        .withMessage("follow-up-1-1")
                        .buildFollowUp()
        );
        Question followUp12 = fixtureFactory.make(
                QuestionBuilder.builder()
                        .withInterviewId(interview.getId())
                        .withParentQuestionId(followUp11.getId())
                        .withDepth(2)
                        .withQuestionStatus(QuestionStatus.ANSWERED)
                        .withMessage("follow-up-1-2")
                        .buildFollowUp()
        );
        Question followUp21 = fixtureFactory.make(
                QuestionBuilder.builder()
                        .withInterviewId(interview.getId())
                        .withParentQuestionId(root2.getId())
                        .withDepth(1)
                        .withQuestionStatus(QuestionStatus.ANSWERED)
                        .withMessage("follow-up-2-1")
                        .buildFollowUp()
        );

        Answer answer1 = fixtureFactory.make(AnswerBuilder.builder().withQuestionId(root1.getId()).withMessage("a1").buildScored());
        Answer answer11 = fixtureFactory.make(AnswerBuilder.builder().withQuestionId(followUp11.getId()).withMessage("a11").buildScored());
        Answer answer12 = fixtureFactory.make(AnswerBuilder.builder().withQuestionId(followUp12.getId()).withMessage("a12").buildScored());
        Answer answer2 = fixtureFactory.make(AnswerBuilder.builder().withQuestionId(root2.getId()).withMessage("a2").buildScored());
        Answer answer21 = fixtureFactory.make(AnswerBuilder.builder().withQuestionId(followUp21.getId()).withMessage("a21").buildScored());

        List<Question> storedQuestions = List.of(root1, root2, followUp11, followUp12, followUp21);
        List<QnA> qnAsInQuestionOrder = List.of(
                qna(root1, answer1),
                qna(root2, answer2),
                qna(followUp11, answer11),
                qna(followUp12, answer12),
                qna(followUp21, answer21)
        );

        given(interviewReader.getAllQuestions(Association.from(interview.getId()))).willReturn(storedQuestions);
        given(answerFinder.findQnA(storedQuestions)).willReturn(qnAsInQuestionOrder);

        InterviewHistoryDetailResponse response = interviewQueryService.viewHistory(interview.getId());

        assertThat(response.qnAResponses())
                .extracting(
                        qna -> qna.displayOrder(),
                        qna -> qna.question()
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("1.", "root-1"),
                        org.assertj.core.groups.Tuple.tuple("1-1.", "follow-up-1-1"),
                        org.assertj.core.groups.Tuple.tuple("1-2.", "follow-up-1-2"),
                        org.assertj.core.groups.Tuple.tuple("2.", "root-2"),
                        org.assertj.core.groups.Tuple.tuple("2-1.", "follow-up-2-1")
                );
    }

    private QnA qna(Question question, Answer answer) {
        return new QnA(question, answer, null, EvaluationAnalysis.empty(List.of()));
    }
}
