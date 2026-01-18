package com.javis.learn_hub.review.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorSortDirection;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ReviewBuilder;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewReaderTest {

    private TestFixtureFactory fixtureFactory = new TestFixtureFactory();
    private ReviewReader reviewReader = new ReviewReader(
            fixtureFactory.getReviewRepository()
    );

    @DisplayName("자신이 작성한 리뷰를 최신순으로 조회한다.")
    @Test
    void testGetAllReviews_Latest() {
        //given
        Member member = fixtureFactory.make(MemberBuilder.builder().build());
        Review oldReview = fixtureFactory.make(ReviewBuilder.builder().withRevieweeId(member.getId()).build());
        Review newReview = fixtureFactory.make(ReviewBuilder.builder().withRevieweeId(member.getId()).build());
        CursorPageRequest cursorPageRequest = CursorPageRequest.builder().withSort(CursorSortDirection.DESC).build();
        List<Review> expected = List.of(newReview, oldReview);

        //when
        List<Review> actual = reviewReader.getAllReviews(member.getId(), cursorPageRequest);

        //then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @DisplayName("자신이 작성한 리뷰를 오래된순으로 조회한다.")
    @Test
    void testGetAllReviews_Oldest() {
        //given
        Member member = fixtureFactory.make(MemberBuilder.builder().build());
        Review oldReview = fixtureFactory.make(ReviewBuilder.builder().withRevieweeId(member.getId()).build());
        Review newReview = fixtureFactory.make(ReviewBuilder.builder().withRevieweeId(member.getId()).build());
        CursorPageRequest cursorPageRequest = CursorPageRequest.builder().withSort(CursorSortDirection.ASC).build();
        List<Review> expected = List.of(oldReview, newReview);

        //when
        List<Review> actual = reviewReader.getAllReviews(member.getId(), cursorPageRequest);

        //then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @DisplayName("[admin용] 모든 리뷰를 상태에 따라 최신순으로 조회한다.")
    @Test
    void testGetAllReviews_Latest_Admin() {
        //given
        Member member1 = fixtureFactory.make(MemberBuilder.builder().build());
        Member member2 = fixtureFactory.make(MemberBuilder.builder().build());
        RegistrationStatus status = RegistrationStatus.PENDING_REVIEW;
        Review oldReview = fixtureFactory.make(ReviewBuilder.builder().withRevieweeId(member1.getId()).withRegistrationStatus(
                status).build());
        Review newReview = fixtureFactory.make(ReviewBuilder.builder().withRevieweeId(member2.getId()).withRegistrationStatus(
                status).build());
        CursorPageRequest cursorPageRequest = CursorPageRequest.builder().withSort(CursorSortDirection.DESC).build();
        List<Review> expected = List.of(newReview, oldReview);

        //when
        List<Review> actual = reviewReader.getAllReviews(cursorPageRequest, status);

        //then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @DisplayName("[admin용] 모든 리뷰를 상태에 따라 오래된순으로 조회한다.")
    @Test
    void testGetAllReviews_Oldest_Admin() {
        //given
        Member member1 = fixtureFactory.make(MemberBuilder.builder().build());
        Member member2 = fixtureFactory.make(MemberBuilder.builder().build());
        RegistrationStatus status = RegistrationStatus.APPROVED;
        Review oldReview = fixtureFactory.make(ReviewBuilder.builder().withRevieweeId(member1.getId()).withRegistrationStatus(
                status).build());
        Review newReview = fixtureFactory.make(ReviewBuilder.builder().withRevieweeId(member2.getId()).withRegistrationStatus(
                status).build());
        CursorPageRequest cursorPageRequest = CursorPageRequest.builder().withSort(CursorSortDirection.ASC).build();
        List<Review> expected = List.of(oldReview, newReview);

        //when
        List<Review> actual = reviewReader.getAllReviews(cursorPageRequest, status);

        //then
        assertThat(actual).containsExactlyElementsOf(expected);
    }
}
