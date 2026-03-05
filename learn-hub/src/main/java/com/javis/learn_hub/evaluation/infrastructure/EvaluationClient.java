package com.javis.learn_hub.evaluation.infrastructure;

import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationAsyncRequest;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationClient {

    private final RestClient restClient;

    @Value("${python-server-uri}")
    private String pythonServerUri;

    @Value("${callback.base-url}")
    private String callbackBaseUrl;

    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 2000)
    )
    public void request(Long answerId, String referenceAnswer,
                        Set<String> keywords, String userAnswer) {
        String callbackUrl = callbackBaseUrl + "/internal/evaluation/callback";

        EvaluationAsyncRequest evaluationRequest = new EvaluationAsyncRequest(
                answerId,
                referenceAnswer,
                keywords,
                userAnswer,
                callbackUrl
        );

        restClient.post()
                .uri(pythonServerUri + "/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(evaluationRequest)
                .retrieve()
                .toBodilessEntity();
        log.info("채점 요청 전송 완료: answerId={}", answerId);
    }

    @Recover
    public void recover(RestClientException e, Long answerId, String referenceAnswer,
                        Set<String> keywords, String userAnswer) {
        log.error("채점 요청 최종 실패 (3회 재시도 소진): answerId={}", answerId, e);
        throw new EvaluationRequestException(answerId);
    }
}
