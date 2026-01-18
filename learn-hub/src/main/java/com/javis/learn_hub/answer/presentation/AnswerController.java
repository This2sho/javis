package com.javis.learn_hub.answer.presentation;

import com.javis.learn_hub.answer.domain.EvaluationResult;
import com.javis.learn_hub.answer.service.AnswerService;
import com.javis.learn_hub.answer.service.dto.AnswerRequest;
import com.javis.learn_hub.interview.service.InterviewCommandService;
import com.javis.learn_hub.interview.service.dto.InterviewerResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import lombok.RequiredArgsConstructor;
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

    private final AnswerService answerService;
    private final InterviewCommandService interviewCommandService;

    @PostMapping("/questions/{questionId}/answer")
    public ResponseEntity<InterviewerResponse> answer(
            @PathVariable Long questionId,
            @RequestBody AnswerRequest request,
            @Authenticated MemberId memberId
    ) {
        EvaluationResult evaluationResult = answerService.answer(questionId, request);
        InterviewerResponse interviewerResponse = interviewCommandService.continueNextQuestion(questionId, evaluationResult.getPreferences());
        return ResponseEntity.ok(interviewerResponse);
    }
}
