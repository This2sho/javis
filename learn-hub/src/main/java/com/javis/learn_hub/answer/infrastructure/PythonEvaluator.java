package com.javis.learn_hub.answer.infrastructure;

import com.javis.learn_hub.answer.domain.EvaluationResult;
import com.javis.learn_hub.answer.domain.Grade;
import com.javis.learn_hub.answer.domain.service.Evaluator;
import com.javis.learn_hub.answer.infrastructure.dto.AnswerFeedbackResponse;
import com.javis.learn_hub.answer.service.dto.EvaluationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class PythonEvaluator implements Evaluator {

    @Value("${python-server-uri}")
    private String pythonServerUri;

    private final RestClient restClient;

    @Override
    public EvaluationResult evaluate(EvaluationRequest request) {
        AnswerFeedbackResponse response = call(request);
        return toEvaluationResult(response);
    }

    private AnswerFeedbackResponse call(EvaluationRequest request) {
        try {
            return restClient.post()
                    .uri(pythonServerUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AnswerFeedbackResponse.class);
        }
        catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("[Evaluator API] Client error: " + e.getStatusCode());
        }
        catch (HttpServerErrorException e) {
            throw new IllegalStateException("[Evaluator API] Server error: " + e.getStatusCode());
        }
        catch (ResourceAccessException e) {
            throw new IllegalStateException("[Evaluator API] Network error", e);
        }
        catch (Exception e) {
            throw new RuntimeException("[Evaluator API] 알지 못하는 예외 발생");
        }
    }

    private EvaluationResult toEvaluationResult(AnswerFeedbackResponse response) {
        Grade grade = toGrade(response.grade());
        return new EvaluationResult(grade, response.feedback());
    }

    private Grade toGrade(String grade) {
        return switch(grade) {
            case "perfect" -> Grade.PERFECT;
            case "good" -> Grade.GOOD;
            case "vague" -> Grade.VAGUE;
            case "incorrect" -> Grade.INCORRECT;
            default -> throw new IllegalArgumentException("Unknown external grade: " + grade);
        };
    }
}
