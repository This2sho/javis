package com.javis.learn_hub.admin.presentation;

import com.javis.learn_hub.member.domain.Role;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.service.ReviewCommandService;
import com.javis.learn_hub.review.service.ReviewQueryService;
import com.javis.learn_hub.review.service.dto.ReviewRequestsResponse;
import com.javis.learn_hub.review.service.dto.ReviewUpdateRequest;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorPageResponse;
import com.javis.learn_hub.support.domain.Authenticated;
import com.javis.learn_hub.support.domain.MemberId;
import com.javis.learn_hub.support.domain.RequireRole;
import com.javis.learn_hub.support.presentation.WithCursor;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/api")
@RestController
public class AdminReviewController {

    private final ReviewQueryService reviewQueryService;
    private final ReviewCommandService reviewCommandService;

    @RequireRole(value = Role.ADMIN)
    @GetMapping("/review-requests")
    public ResponseEntity<CursorPageResponse<ReviewRequestsResponse>> viewReviewRequests(
            @RequestParam(defaultValue = "PENDING_REVIEW") RegistrationStatus registrationStatus,
            @WithCursor(requiredTargetId = false, requiredTargetTime = false) CursorPageRequest cursorPageRequest,
            @Authenticated MemberId memberId
    ) {
        CursorPageResponse<ReviewRequestsResponse> result = reviewQueryService.viewReviewRequests(cursorPageRequest, registrationStatus);
        return ResponseEntity.ok(result);
    }

    @RequireRole(value = Role.ADMIN)
    @PutMapping("/review-requests/{reviewId}")
    public ResponseEntity<Void> updateReviewRequests(
            @RequestBody ReviewUpdateRequest reviewUpdateRequest,
            @Authenticated MemberId memberId,
            @PathVariable Long reviewId
    ) {
        reviewCommandService.update(reviewId, reviewUpdateRequest);
        return ResponseEntity.noContent()
                .location(URI.create("/admin/review-requests"))
                .build();
    }
}
