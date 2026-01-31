package com.javis.learn_hub.evaluation.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.evaluation.domain.EvaluationStatus;
import com.javis.learn_hub.evaluation.domain.repository.EvaluationRepository;
import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.event.EvaluationCompletedEvent;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EvaluationProcessorTest {

    private EvaluationRepository evaluationRepository;
    private EvaluationProcessor evaluationProcessor;

    @BeforeEach
    void setUp() {
        evaluationRepository = mock(EvaluationRepository.class);
        evaluationProcessor = new EvaluationProcessor(evaluationRepository);
    }

    @Test
    @DisplayName("채점 완료 시 Evaluation이 생성되고 EvaluationCompletedEvent가 반환된다")
    void complete_createsEvaluationAndReturnsEvent() {
        Long answerId = 10L;
        Long questionId = 1L;
        Long memberId = 100L;
        String grade = "GOOD";
        String feedback = "잘 설명했습니다.";

        List<DomainEvent> events = evaluationProcessor.complete(
                answerId, questionId, memberId, grade, feedback
        );

        ArgumentCaptor<Evaluation> evaluationCaptor = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationRepository).save(evaluationCaptor.capture());

        Evaluation savedEvaluation = evaluationCaptor.getValue();
        assertThat(savedEvaluation.getStatus()).isEqualTo(EvaluationStatus.COMPLETED);
        assertThat(savedEvaluation.getAnswerId().getId()).isEqualTo(answerId);

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(EvaluationCompletedEvent.class);

        EvaluationCompletedEvent event = (EvaluationCompletedEvent) events.get(0);
        assertThat(event.answerId()).isEqualTo(answerId);
        assertThat(event.questionId()).isEqualTo(questionId);
        assertThat(event.memberId()).isEqualTo(memberId);
        assertThat(event.preferences()).isNotEmpty();
    }
}
