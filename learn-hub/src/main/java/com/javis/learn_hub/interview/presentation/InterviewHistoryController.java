package com.javis.learn_hub.interview.presentation;

import com.javis.learn_hub.interview.service.InterviewQueryService;
import com.javis.learn_hub.interview.service.dto.InterviewHistoryDetailResponse;
import com.javis.learn_hub.interview.service.dto.InterviewHistoryResponse;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorPageResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import com.javis.learn_hub.support.presentation.WithCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class InterviewHistoryController {

    private final InterviewQueryService interviewQueryService;

    @GetMapping("/interviews")
    public ResponseEntity<CursorPageResponse<InterviewHistoryResponse>> viewHistories(
            @WithCursor(requiredTargetId = false, requiredTargetTime = false) CursorPageRequest cursorPageRequest,
            @Authenticated MemberId memberId
    ) {
        CursorPageResponse<InterviewHistoryResponse> result = interviewQueryService.viewHistories(
                cursorPageRequest, memberId.getId());
        return ResponseEntity.ok(result);
    }

    // todo Answer 도메인 쪽으로 변경
    @GetMapping("/interviews/{interviewId}")
    public ResponseEntity<InterviewHistoryDetailResponse> viewInterviewDetail(
            @PathVariable Long interviewId,
            @Authenticated MemberId memberId
    ) {
        InterviewHistoryDetailResponse response = interviewQueryService.viewHistory(
                interviewId);
        return ResponseEntity.ok(response);
    }
}
