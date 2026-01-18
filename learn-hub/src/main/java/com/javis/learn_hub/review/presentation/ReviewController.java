package com.javis.learn_hub.review.presentation;

import com.javis.learn_hub.review.service.ReviewCommandService;
import com.javis.learn_hub.review.service.ReviewQueryService;
import com.javis.learn_hub.review.service.dto.ReviewRequestsResponse;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorPageResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import com.javis.learn_hub.support.presentation.WithCursor;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping("/problems/{problemId}/review-requests")
    public ResponseEntity<Void> reviewRequests(
            @Authenticated MemberId memberId,
            @PathVariable Long problemId
    ) {
        reviewCommandService.register(memberId.getId(), problemId);
        return ResponseEntity.created(URI.create("/review-requests")).build();
    }

    @GetMapping("/review-requests")
    public ResponseEntity<CursorPageResponse<ReviewRequestsResponse>> viewReviewRequests(
            @WithCursor(requiredTargetId = false, requiredTargetTime = false) CursorPageRequest cursorPageRequest,
            @Authenticated MemberId memberId
    ) {
        CursorPageResponse<ReviewRequestsResponse> result = reviewQueryService.viewReviewRequests(memberId.getId(), cursorPageRequest);
        return ResponseEntity.ok(result);
    }
}
