package com.javis.learn_hub.evaluation.infrastructure;

import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationAsyncRequest;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationClient {

    private final RestClient restClient;

    @Value("${python-server-uri}")
    private String pythonServerUri;

    @Value("${callback.base-url}")
    private String callbackBaseUrl;

    @Async
    public void requestAsync(Long answerId, String referenceAnswer,
                             Set<String> keywords, String userAnswer) {
        String callbackUrl = callbackBaseUrl + "/internal/evaluation/callback";

        EvaluationAsyncRequest request = new EvaluationAsyncRequest(
                answerId,
                referenceAnswer,
                keywords,
                userAnswer,
                callbackUrl
        );

        try {
            restClient.post()
                    .uri(pythonServerUri + "/evaluate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("채점 요청 전송 완료: answerId={}", answerId);
        } catch (Exception e) {
            log.error("채점 요청 실패: answerId={}", answerId, e);
        }
    }
}
