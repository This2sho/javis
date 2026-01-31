package com.javis.learn_hub.answer.presentation;

import com.javis.learn_hub.answer.service.AnswerCommandService;
import com.javis.learn_hub.answer.service.AnswerQueryService;
import com.javis.learn_hub.answer.service.dto.AnswerRequest;
import com.javis.learn_hub.answer.service.dto.AnswerStatusResponse;
import com.javis.learn_hub.answer.service.dto.AnswerSubmitResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AnswerController {

    private final AnswerCommandService answerCommandService;
    private final AnswerQueryService answerQueryService;

    /**
     * 비동기 답변 제출 API
     * 즉시 202 Accepted 응답 후 채점 결과는 WebSocket으로 전송
     */
    @PostMapping("/questions/{questionId}/answer")
    public ResponseEntity<AnswerSubmitResponse> submitAnswer(
            @PathVariable Long questionId,
            @RequestBody AnswerRequest request,
            @Authenticated MemberId memberId
    ) {
        AnswerSubmitResponse response = answerCommandService.submitAnswer(questionId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * 답변 상태 조회 API
     * 채점 완료 여부 및 결과 확인
     */
    @GetMapping("/answers/{answerId}")
    public ResponseEntity<AnswerStatusResponse> getAnswerStatus(@PathVariable Long answerId) {
        AnswerStatusResponse response = answerQueryService.getAnswerStatus(answerId);
        return ResponseEntity.ok(response);
    }
}
