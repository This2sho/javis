package com.javis.learn_hub.review.service.dto;

import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.review.domain.Review;

public record ReviewRequestsResponse(
        Long id,
        Long problemId,
        String rootProblem,
        String registrationStatus
) {

    public static ReviewRequestsResponse of(Review review, Problem problem) {
        return new ReviewRequestsResponse(review.getId(), problem.getId(), problem.getContent(),
                review.getRegistrationStatus().name());
    }
}
