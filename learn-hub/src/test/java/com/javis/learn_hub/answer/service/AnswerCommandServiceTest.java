package com.javis.learn_hub.answer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.answer.service.dto.AnswerRequest;
import com.javis.learn_hub.answer.service.dto.AnswerSubmitResponse;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

class AnswerCommandServiceTest {

    private AnswerProcessor answerProcessor;
    private ApplicationEventPublisher eventPublisher;
    private AnswerCommandService answerCommandService;

    @BeforeEach
    void setUp() {
        answerProcessor = mock(AnswerProcessor.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        answerCommandService = new AnswerCommandService(answerProcessor, eventPublisher);
    }

    @Test
    @DisplayName("답변 제출 시 Answer가 생성되고 AnswerCreatedEvent가 발행된다")
    void submitAnswer_createsAnswerAndPublishesEvent() {
        Long questionId = 1L;
        String problem = "REST API란?";
        String userAnswer = "REST는 Representational State Transfer의 약자입니다.";
        AnswerRequest request = new AnswerRequest(problem, userAnswer);

        AnswerCreatedEvent mockEvent = new AnswerCreatedEvent(10L, questionId, userAnswer);
        when(answerProcessor.create(questionId, userAnswer)).thenReturn(mockEvent);

        AnswerSubmitResponse response = answerCommandService.submitAnswer(questionId, request);
        assertThat(response.status()).isEqualTo("PENDING");

        ArgumentCaptor<AnswerCreatedEvent> eventCaptor = ArgumentCaptor.forClass(AnswerCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        AnswerCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.answerId()).isEqualTo(10L);
        assertThat(capturedEvent.questionId()).isEqualTo(questionId);
        assertThat(capturedEvent.userAnswer()).isEqualTo(userAnswer);
    }
}
