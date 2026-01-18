package com.javis.learn_hub.interview.presentation;

import com.javis.learn_hub.interview.service.InterviewCommandService;
import com.javis.learn_hub.interview.service.dto.QuestionResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class InterviewController {

    private final InterviewCommandService interviewCommandService;

    @GetMapping("/interviews/start/{mainCategory}")
    public ResponseEntity<QuestionResponse> startInterview(
            @PathVariable String mainCategory,
            @Authenticated MemberId memberId
    ) {
        QuestionResponse questionResponse = interviewCommandService.start(mainCategory, memberId.getId());
        return ResponseEntity.ok(questionResponse);
    }
}
