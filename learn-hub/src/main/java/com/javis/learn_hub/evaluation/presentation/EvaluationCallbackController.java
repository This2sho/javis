package com.javis.learn_hub.evaluation.presentation;

import com.javis.learn_hub.evaluation.application.EvaluationService;
import com.javis.learn_hub.evaluation.application.dto.EvaluationCallbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/internal/evaluation")
@RestController
public class EvaluationCallbackController {

    private final EvaluationService evaluationService;

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody EvaluationCallbackRequest request) {
        log.info("채점 콜백 수신: answerId={}, grade={}", request.answerId(), request.grade());
        evaluationService.completeEvaluation(request);
        return ResponseEntity.ok().build();
    }
}