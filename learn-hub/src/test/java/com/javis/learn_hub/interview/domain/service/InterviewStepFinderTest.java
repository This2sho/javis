package com.javis.learn_hub.interview.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.evaluation.domain.service.EvaluationReader;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.interview.domain.service.dto.InterviewStepResult;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.AnswerBuilder;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.builder.QuestionBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewStepFinderTest {

    private final TestFixtureFactory fixtureFactory = new TestFixtureFactory();

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private InterviewReader interviewReader;

    @Mock
    private AnswerReader answerReader;

    @Mock
    private EvaluationReader evaluationReader;

    @DisplayName("SCORING 상태 답변으로 재진입하면 채점 대기 상태로 이어간다.")
    @Test
    void testFindWhenAnswerIsScoring() {
        InterviewStepFinder interviewStepFinder = new InterviewStepFinder(
                interviewRepository,
                interviewReader,
                answerReader,
                evaluationReader
        );
        Interview interview = fixtureFactory.make(InterviewBuilder.builder().build());
        Question question = fixtureFactory.make(
                QuestionBuilder.builder()
                        .withInterviewId(interview.getId())
                        .withQuestionStatus(QuestionStatus.ANSWERED)
                        .buildRoot()
        );
        Answer answer = AnswerBuilder.builder()
                .withQuestionId(question.getId())
                .build();
        answer.toScoring();

        given(interviewReader.getCurrentQuestion(interview)).willReturn(question);
        given(answerReader.getByQuestionId(question.getId())).willReturn(answer);

        InterviewStepResult actual = interviewStepFinder.find(interview);

        assertThat(actual).isEqualTo(InterviewStepResult.pendingEvaluation(question));
        verifyNoInteractions(evaluationReader);
    }
}
