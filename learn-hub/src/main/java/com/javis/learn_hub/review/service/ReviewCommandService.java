package com.javis.learn_hub.review.service;

import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.review.domain.service.ReviewProcessor;
import com.javis.learn_hub.review.domain.service.ReviewReader;
import com.javis.learn_hub.review.service.dto.ReviewUpdateRequest;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReviewCommandService {

    private final ReviewReader reviewReader;
    private final ReviewProcessor reviewProcessor;
    private final ProblemReader problemReader;
    private final ApplicationEventPublisher applicationEventPublisher;

    public Long register(Long memberId, Long problemId) {
        Problem problem = problemReader.get(problemId);
        problem.validateWriter(Association.from(memberId));
        Review review = reviewProcessor.create(Association.from(problemId), Association.from(memberId));
        return review.getId();
    }

    @Transactional
    public void update(Long reviewId, ReviewUpdateRequest request) {
        RegistrationStatus status = RegistrationStatus.from(request.registrationStatus());
        Review review = reviewReader.get(reviewId);
        List<DomainEvent> events = reviewProcessor.update(review, status);
        events.forEach(applicationEventPublisher::publishEvent);
    }
}
