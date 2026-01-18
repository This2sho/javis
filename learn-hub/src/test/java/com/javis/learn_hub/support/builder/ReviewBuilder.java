package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.support.domain.Association;

public class ReviewBuilder {

    private Association<Problem> rootProblemId = Association.from(1L);
    private Association<Member> revieweeId = Association.from(1L);
    private RegistrationStatus registrationStatus = RegistrationStatus.PENDING_REVIEW;

    public static ReviewBuilder builder() {
        return new ReviewBuilder();
    }

    public ReviewBuilder withRevieweeId(Long memberId) {
        this.revieweeId = Association.from(memberId);
        return this;
    }

    public ReviewBuilder withRevieweeId(Association<Member> memberId) {
        this.revieweeId = memberId;
        return this;
    }

    public ReviewBuilder withRootProblemId(Long rootProblemId) {
        this.rootProblemId = Association.from(rootProblemId);
        return this;
    }

    public ReviewBuilder withRootProblemId(Association<Problem> rootProblemId) {
        this.rootProblemId = rootProblemId;
        return this;
    }

    public ReviewBuilder withRegistrationStatus(RegistrationStatus status) {
        this.registrationStatus = status;
        return this;
    }

    public Review build() {
        Review review = new Review(rootProblemId, revieweeId, registrationStatus);
        return review;
    }
}

