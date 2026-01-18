package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.review.domain.repository.ReviewRepository;
import com.javis.learn_hub.support.domain.Association;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.data.domain.Pageable;

public class InMemoryReviewRepository extends InMemoryRepository<Review> implements ReviewRepository {

    @Override
    public List<Review> findAllByRevieweeIdByLatest(LocalDateTime targetTime, Long targetId,
                                                    Association<Member> revieweeId, Pageable pageable) {
        Predicate<Review> cursorCondition = review ->
                review.getRevieweeId().equals(revieweeId) &&
                        (review.getUpdatedAt().isBefore(targetTime)
                                || (review.getUpdatedAt().isEqual(targetTime)
                                && review.getId() <= targetId));
        Comparator<Review> latestOrder = Comparator.comparing(Review::getUpdatedAt).reversed()
                .thenComparing(Review::getId, Comparator.reverseOrder());
        int pageSize = pageable.getPageSize();
        return findAll(cursorCondition)
                .stream()
                .sorted(latestOrder)
                .limit(pageSize)
                .toList();
    }

    @Override
    public List<Review> findAllByRevieweeIdByOldest(LocalDateTime targetTime, Long targetId,
                                                    Association<Member> revieweeId, Pageable pageable) {
        Predicate<Review> cursorCondition = review -> review.getRevieweeId().equals(revieweeId)
                && (review.getUpdatedAt().isAfter(targetTime)
                || (review.getUpdatedAt().isEqual(targetTime)
                && review.getId() >= targetId));
        Comparator<Review> oldestOrder = Comparator.comparing(Review::getUpdatedAt)
                .thenComparing(Review::getId);
        int pageSize = pageable.getPageSize();
        return findAll(cursorCondition)
                .stream()
                .sorted(oldestOrder)
                .limit(pageSize)
                .toList();
    }

    @Override
    public List<Review> findAllByRegistrationStatusByLatest(LocalDateTime targetTime, Long targetId,
                                                            RegistrationStatus registrationStatus, Pageable pageable) {
        Predicate<Review> cursorCondition = review ->
                review.getRegistrationStatus().equals(registrationStatus) &&
                        (review.getUpdatedAt().isBefore(targetTime)
                                || (review.getUpdatedAt().isEqual(targetTime)
                                && review.getId() <= targetId));
        Comparator<Review> latestOrder = Comparator.comparing(Review::getUpdatedAt).reversed()
                .thenComparing(Review::getId, Comparator.reverseOrder());
        int pageSize = pageable.getPageSize();
        return findAll(cursorCondition)
                .stream()
                .sorted(latestOrder)
                .limit(pageSize)
                .toList();
    }

    @Override
    public List<Review> findAllByRegistrationStatusByOldest(LocalDateTime targetTime, Long targetId,
                                                            RegistrationStatus registrationStatus, Pageable pageable) {
        Predicate<Review> cursorCondition = review -> review.getRegistrationStatus().equals(registrationStatus)
                && (review.getUpdatedAt().isAfter(targetTime)
                || (review.getUpdatedAt().isEqual(targetTime)
                && review.getId() >= targetId));
        Comparator<Review> oldestOrder = Comparator.comparing(Review::getUpdatedAt)
                .thenComparing(Review::getId);
        int pageSize = pageable.getPageSize();
        return findAll(cursorCondition)
                .stream()
                .sorted(oldestOrder)
                .limit(pageSize)
                .toList();
    }

    @Override
    public boolean existsByRootProblemId(Association<Problem> rootProblemId) {
        return findOne(review -> review.getRootProblemId().equals(rootProblemId)).isPresent();
    }
}
