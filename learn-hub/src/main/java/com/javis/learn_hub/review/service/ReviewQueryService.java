package com.javis.learn_hub.review.service;

import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.review.domain.service.ReviewFinder;
import com.javis.learn_hub.review.domain.service.ReviewReader;
import com.javis.learn_hub.review.service.dto.ReviewRequestsResponse;
import com.javis.learn_hub.support.application.CursorPagingSupport;
import com.javis.learn_hub.support.application.dto.CursorPage;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorPageResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReviewQueryService {

    private final ReviewReader reviewReader;
    private final ReviewFinder reviewFinder;

    public CursorPageResponse<ReviewRequestsResponse> viewReviewRequests(Long memberId, CursorPageRequest cursorPageRequest) {
        List<Review> reviews = reviewReader.getAllReviews(memberId, cursorPageRequest);
        CursorPage<Review> slicedReviews = CursorPagingSupport.slice(reviews, cursorPageRequest);
        Map<Long, Problem> problemsByReviewId = reviewFinder.getAllProblem(slicedReviews.content());
        return collectToResponse(slicedReviews, problemsByReviewId);
    }

    /**
     * Admin용 api
     */
    public CursorPageResponse<ReviewRequestsResponse> viewReviewRequests(CursorPageRequest cursorPageRequest,
                                                                         RegistrationStatus registrationStatus) {
        List<Review> reviews = reviewReader.getAllReviews(cursorPageRequest, registrationStatus);
        CursorPage<Review> slicedReviews = CursorPagingSupport.slice(reviews, cursorPageRequest);
        Map<Long, Problem> problemsByReviewId = reviewFinder.getAllProblem(slicedReviews.content());
        return collectToResponse(slicedReviews, problemsByReviewId);
    }

    private CursorPageResponse<ReviewRequestsResponse> collectToResponse(CursorPage<Review> slicedReviews,
                                                                         Map<Long, Problem> problemsByReviewId) {
        List<ReviewRequestsResponse> responses = slicedReviews.content()
                .stream()
                .map(review -> ReviewRequestsResponse.of(review, problemsByReviewId.get(review.getId())))
                .toList();
        return new CursorPageResponse<>(responses, slicedReviews.nextCursor(), slicedReviews.hasNext());
    }

    public Long getRootProblemId(Long reviewId) {
        Review review = reviewReader.get(reviewId);
        return review.getRootProblemId().getId();
    }
}
