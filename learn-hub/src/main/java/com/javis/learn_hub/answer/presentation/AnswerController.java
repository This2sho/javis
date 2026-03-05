package com.javis.learn_hub.answer.presentation;

import com.javis.learn_hub.answer.service.AnswerCommandService;
import com.javis.learn_hub.answer.service.dto.AnswerRequest;
import com.javis.learn_hub.answer.service.dto.AnswerSubmitResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
