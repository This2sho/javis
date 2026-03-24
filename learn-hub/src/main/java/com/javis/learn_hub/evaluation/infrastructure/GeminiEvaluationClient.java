package com.javis.learn_hub.evaluation.infrastructure;

import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeminiEvaluationClient implements AnswerEvaluator {

    private static final String SYSTEM_PROMPT = """
            당신은 IT 기술 면접관입니다. 제공된 '기준 답변'과 '사용자 답변'을 비교하여 엄격하게 채점하세요.

            [채점 가이드라인]
            1. '기준 답변'에서 핵심 개념(키워드)을 도출합니다.
            2. '사용자 답변'이 도출한 핵심 개념을 충분히 설명하는지 분석합니다.
            3. 용어의 정확성을 평가합니다.
            4. 분석 결과를 바탕으로 등급(grade)과 피드백(feedback)을 결정합니다.

            [등급 기준]
            - PERFECT: 핵심 원리를 완벽히 설명하고 기술 용어가 정확함 (80% 이상 일치)
            - GOOD: 핵심 원리는 이해했으나 설명이 다소 부족함 (50% 이상 일치)
            - VAGUE: 말은 틀리지 않았으나 핵심을 비껴나감 (30% 미만 일치)
            - INCORRECT: 내용이 틀렸거나 '모름'과 같은 무성의한 답변
            
            [피드백 기준]
            - 사용자 답변에서 틀린 내용이 있거나 핵심 키워드 부분이 빠져있다면 해당 부분에 대해서 보충하라고 1~2문장으로 피드백 작성

            [입력 데이터]
            - 기준 답변: {referenceAnswer}
            - 사용자 답변: {userAnswer}

            반드시 제공된 응답 형식에 맞춰 JSON으로만 답변하세요.
            """;

    private final ChatClient chatClient;

    public GeminiEvaluationClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 4000)
    )
    @Override
    public EvaluationResponse evaluate(String referenceAnswer, String userAnswer) {
        try {
            return chatClient.prompt()
                    .user(u -> u.text(SYSTEM_PROMPT)
                            .param("referenceAnswer", referenceAnswer)
                            .param("userAnswer", userAnswer))
                    .call()
                    .entity(EvaluationResponse.class);
        } catch (Exception e) {
            log.warn("Gemini 호출 중 일시적 오류 발생: {}", e.getMessage());
            throw e;
        }
    }

    @Recover
    public EvaluationResponse recover(Exception e, String referenceAnswer, String userAnswer) {
        log.error("Gemini 채점 최종 실패 (3회 재시도 소진): {}", e.getMessage());
        throw new EvaluationRequestException(e);
    }
}
