package com.javis.learn_hub.evaluation.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.evaluation.domain.analysis.SentenceSegmenter;
import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.evaluation.domain.repository.EvaluationRepository;
import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import com.javis.learn_hub.event.EvaluationCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EvaluationProcessorTest {

    private EvaluationRepository evaluationRepository;
    private AnswerReader answerReader;
    private EvaluationProcessor evaluationProcessor;

    @BeforeEach
    void setUp() {
        evaluationRepository = mock(EvaluationRepository.class);
        answerReader = mock(AnswerReader.class);
        evaluationProcessor = new EvaluationProcessor(
                evaluationRepository,
                answerReader,
                new SentenceSegmenter(),
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("채점 완료 시 Evaluation이 생성되고 EvaluationCompletedEvent가 반환된다")
    void complete_createsEvaluationAndReturnsEvent() {
        Long answerId = 10L;
        Long questionId = 1L;
        EvaluationResponse response = new EvaluationResponse("판단 근거", "GOOD", "잘 설명했습니다.", java.util.List.of(), java.util.List.of());
        given(answerReader.get(answerId)).willReturn(Answer.create(questionId, "테스트 답변입니다."));
        EvaluationCompletedEvent event = evaluationProcessor.complete(
                answerId, questionId, response
        );

        ArgumentCaptor<Evaluation> evaluationCaptor = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationRepository).save(evaluationCaptor.capture());

        Evaluation savedEvaluation = evaluationCaptor.getValue();
        assertThat(savedEvaluation.getAnswerId().getId()).isEqualTo(answerId);

        assertThat(event.answerId()).isEqualTo(answerId);
        assertThat(event.questionId()).isEqualTo(questionId);
        assertThat(event.preferences()).isNotEmpty();
    }
}
