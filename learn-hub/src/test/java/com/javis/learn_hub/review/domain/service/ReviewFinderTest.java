package com.javis.learn_hub.review.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.builder.ReviewBuilder;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewFinderTest {

    private TestFixtureFactory fixtureFactory = new TestFixtureFactory();
    private ReviewFinder reviewFinder = new ReviewFinder(
            new ProblemReader(
                    fixtureFactory.getProblemRepository(),
                    fixtureFactory.getProblemScoringInfoRepository()
            )
    );

    @DisplayName("리뷰에 해당하는 모든 문제들을 리뷰 아이디로 분류해서 가져온다.")
    @Test
    void testGetAllProblem() {
        //given
        Member member = fixtureFactory.make(MemberBuilder.builder().build());
        Problem problem1 = fixtureFactory.make(ProblemBuilder.builder().build());
        Problem problem2 = fixtureFactory.make(ProblemBuilder.builder().build());
        Review review1 = fixtureFactory.make(
                ReviewBuilder.builder().withRevieweeId(member.getId()).withRootProblemId(problem1.getId()).build());
        Review review2 = fixtureFactory.make(
                ReviewBuilder.builder().withRevieweeId(member.getId()).withRootProblemId(problem2.getId()).build());
        List<Review> reviews = List.of(review1, review2);
        Map<Long, Problem> expected = Map.of(review1.getId(), problem1, review2.getId(), problem2);

        //when
        Map<Long, Problem> actual = reviewFinder.getAllProblem(reviews);

        //then
        assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
    }
}
