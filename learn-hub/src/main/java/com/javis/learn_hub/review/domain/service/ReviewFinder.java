package com.javis.learn_hub.review.domain.service;

import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.review.domain.Review;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ReviewFinder {

    private final ProblemReader problemReader;

    @Transactional(readOnly = true)
    public Map<Long, Problem> getAllProblem(List<Review> reviews) {
        Set<Long> problemIds = getProblemIds(reviews);
        Map<Long, Problem> problemsById = problemReader.getAll(problemIds)
                .stream()
                .collect(Collectors.toMap(
                        Problem::getId,
                        Function.identity()
                ));
        return reviews.stream()
                .collect(Collectors.toMap(Review::getId,
                        review -> problemsById.get(review.getRootProblemId().getId())));
    }

    private Set<Long> getProblemIds(List<Review> reviews) {
        return reviews.stream()
                .map(review -> review.getRootProblemId().getId())
                .collect(Collectors.toSet());
    }
}
