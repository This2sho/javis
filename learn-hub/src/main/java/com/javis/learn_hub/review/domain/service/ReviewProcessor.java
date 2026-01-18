package com.javis.learn_hub.review.domain.service;

import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.event.ReviewApprovedEvent;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.review.domain.repository.ReviewRepository;
import com.javis.learn_hub.support.domain.Association;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReviewProcessor {

    private final ReviewRepository reviewRepository;
    private final ReviewReader reviewReader;

    public Review create(Association<Problem> problemId, Association<Member> writerId) {
        if (reviewReader.exists(problemId)) {
            throw new IllegalStateException("이미 리뷰가 존재합니다.");
        }
        Review review = new Review(problemId, writerId, RegistrationStatus.PENDING_REVIEW);
        reviewRepository.save(review);
        return review;
    }

    public List<DomainEvent> update(Review review, RegistrationStatus newStatus) {
        RegistrationStatus previousStatus = review.getRegistrationStatus();
        review.update(newStatus);
        List<DomainEvent> events = new ArrayList<>();
        if (!previousStatus.isApproved() && newStatus.isApproved()) {
            events.add(new ReviewApprovedEvent(review.getRootProblemId().getId()));
        }
        return events;
    }
}
