package com.javis.learn_hub.evaluation.infrastructure;

import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeminiEvaluationClient implements AnswerEvaluator {

    private static final Pattern HANGUL_PATTERN = Pattern.compile("[가-힣]");
    private static final Pattern LATIN_PATTERN = Pattern.compile("[A-Za-z]");

    private static final String SYSTEM_PROMPT = """
            당신은 IT 기술 면접관입니다. 채점 시작 전, [질문]의 성격을 먼저 분류하고 해당 모드에 따라 채점하세요.
            
            1. 기술 지식형 (예: TCP, JVM, DB 인덱스 등):
               - 기준: [기준 답변]은 최소 가이드라인입니다.
               - AI 판단: [사용자 답변]이 [기준 답변]보다 기술적으로 더 정확하고 깊이 있다면, [기준 답변]에 없는 내용이라도 적극 반영하여 PERFECT를 부여하세요.
            
            2. 경험/협업형 (예: 어려웠던 점, 갈등 해결 등):
               - 기준: [기준 답변]에 명시된 필수 포함 요소(예: 상황, 행동, 결과 등)를 절대적 기준으로 삼습니다.
               - AI 판단: [사용자 답변]이 아무리 유려해도 [기준 답변]에서 요구하는 핵심 경험의 맥락에서 벗어나면 감점하세요.
            
            [채점 가이드라인]
            - 먼저 'evaluationLogic' 필드에 질문 유형 분류와 채점 근거를 논리적으로 작성하세요.
            - 해당 근거를 바탕으로 최종 등급(grade)와 피드백(feedback)을 결정하세요.
       
            [등급 기준]
            - PERFECT: 핵심 원리를 완벽히 설명하고 기술 용어가 정확함
            - GOOD: 핵심 원리는 이해했으나 설명이 다소 부족함
            - VAGUE: 말은 틀리지 않았으나 핵심을 비껴나감
            - INCORRECT: 내용이 틀렸거나 무성의한 답변
            
            [피드백 기준]
            - PERFECT: 어떤 부분이 좋았는지 피드백 1줄
            - GOOD: 부족했던 개념 or 추가하면 좋을 거 같다는 피드백 1줄
            - VAGUE: 핵심이 무엇인지 설명하는 피드백 1줄
            - INCORRECT: 왜 틀렸는지 설명하는 피드백 1줄

            [언어별 추가 지침]
            {languageEvaluationGuide}

            반드시 제공된 응답 형식에 맞춰 JSON으로만 답변하세요.
            """;

    private static final String USER_PROMPT = """
        [질문]
        {question}

        [기준 답변]
        {referenceAnswer}

        [사용자 답변]
        {userAnswer}
        """;

    private static final String KOREAN_EVALUATION_GUIDE = """
            - 이 문항은 한국어 답변 인터뷰입니다.
            - 내용의 정확성, 구조, 핵심 포함 여부를 우선 평가하세요.
            - 단순한 맞춤법이나 띄어쓰기보다 전달력과 논리성을 더 중요하게 보세요.
            - feedback은 한국어로 작성하세요.
            """;

    private static final String ENGLISH_EVALUATION_GUIDE = """
            - 이 문항은 영어 답변 인터뷰입니다.
            - 내용의 적절성과 함께 영어 문법, 시제, 어휘 선택, collocation, 표현의 자연스러움을 함께 평가하세요.
            - 사소한 문법 실수만으로 곧바로 INCORRECT를 주지 말고, 내용이 충분하면 PERFECT 또는 GOOD 범위 안에서 표현 완성도를 함께 반영하세요.
            - 답변이 질문 의도에는 맞더라도 어색한 문장, 부자연스러운 단어 선택, 반복되는 표현이 있으면 피드백에 구체적으로 적으세요.
            - 영어 질문인데 한국어로 답하거나 영어 문장으로 보기 어려운 수준이면 감점하세요.
            - feedback은 반드시 한국어로 작성하되, 내용 측면과 영어 표현 측면을 함께 짧게 언급하세요.
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
    public EvaluationResponse evaluate(String question, String referenceAnswer, String userAnswer) {
        try {
            String languageEvaluationGuide = isEnglishInterview(question, referenceAnswer)
                    ? ENGLISH_EVALUATION_GUIDE
                    : KOREAN_EVALUATION_GUIDE;
            return chatClient.prompt()
                    .system(s -> s.text(SYSTEM_PROMPT)
                            .param("languageEvaluationGuide", languageEvaluationGuide))
                    .user(u -> u.text(USER_PROMPT)
                            .param("question", question)
                            .param("referenceAnswer", referenceAnswer)
                            .param("userAnswer", userAnswer))
                    .options(ChatOptions.builder()
                            .temperature(0.1)
                            .build())
                    .call()
                    .entity(EvaluationResponse.class);
        } catch (Exception e) {
            log.warn("Gemini 호출 중 일시적 오류 발생: {}", e.getMessage());
            throw e;
        }
    }

    @Recover
    public EvaluationResponse recover(Exception e, String question, String referenceAnswer, String userAnswer) {
        log.error("Gemini 채점 최종 실패 (3회 재시도 소진): {}", e.getMessage());
        throw new EvaluationRequestException(e);
    }

    private boolean isEnglishInterview(String question, String referenceAnswer) {
        String source = (question == null ? "" : question) + "\n" + (referenceAnswer == null ? "" : referenceAnswer);
        int latinCount = countMatches(LATIN_PATTERN, source);
        int hangulCount = countMatches(HANGUL_PATTERN, source);

        if (latinCount < 20) {
            return false;
        }
        return hangulCount == 0 || latinCount >= hangulCount * 3;
    }

    private int countMatches(Pattern pattern, String text) {
        return (int) pattern.matcher(text).results().count();
    }
}
