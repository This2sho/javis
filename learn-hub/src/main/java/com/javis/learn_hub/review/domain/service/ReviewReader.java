package com.javis.learn_hub.review.domain.service;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.review.domain.repository.ReviewRepository;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReviewReader {

    private final ReviewRepository reviewRepository;

    public List<Review> getAllReviews(Long memberId, CursorPageRequest pageRequest) {
        Association<Member> member = Association.from(memberId);
        if (pageRequest.isDesc()) {
            return reviewRepository.findAllByRevieweeIdByLatest(pageRequest.getTargetTime(), pageRequest.getTargetId(),
                    member, pageRequest.getPageable());
        }
        return reviewRepository.findAllByRevieweeIdByOldest(pageRequest.getTargetTime(), pageRequest.getTargetId(),
                member, pageRequest.getPageable());
    }

    /**
     * admin용
     */
    public List<Review> getAllReviews(CursorPageRequest pageRequest, RegistrationStatus status) {
        if (pageRequest.isDesc()) {
            return reviewRepository.findAllByRegistrationStatusByLatest(pageRequest.getTargetTime(), pageRequest.getTargetId(),
                    status, pageRequest.getPageable());
        }
        return reviewRepository.findAllByRegistrationStatusByOldest(pageRequest.getTargetTime(), pageRequest.getTargetId(),
                status, pageRequest.getPageable());
    }

    public Review get(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰입니다."));
    }

    public boolean exists(Association<Problem> problemId) {
        return reviewRepository.existsByRootProblemId(problemId);
    }
}
