package com.javis.learn_hub.review.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.event.ReviewApprovedEvent;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.review.domain.RegistrationStatus;
import com.javis.learn_hub.review.domain.Review;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import com.javis.learn_hub.support.builder.ReviewBuilder;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewProcessorTest {

    private TestFixtureFactory fixtureFactory = new TestFixtureFactory();
    private ReviewProcessor reviewProcessor = new ReviewProcessor(
            fixtureFactory.getReviewRepository(),
            new ReviewReader(fixtureFactory.getReviewRepository())
    );

    @DisplayName("리뷰 생성 시 이미 존재하면 예외가 발생한다.")
    @Test
    void testCreate() {
        //given
        Member writer = fixtureFactory.make(MemberBuilder.builder().build());
        Problem problem = fixtureFactory.make(ProblemBuilder.builder().withWriterId(writer.getId()).build());
        Review review = fixtureFactory.make(
                ReviewBuilder.builder().withRevieweeId(writer.getId()).withRootProblemId(problem.getId()).build());

        //when, then
        assertThatThrownBy(
                () -> reviewProcessor.create(Association.from(problem.getId()), Association.from(writer.getId()))
        );
    }

    @DisplayName("리뷰 업데이트 시 상태가 승인으로 바뀌면 리뷰 승인 이벤트를 생성한다.")
    @Test
    void testUpdate() {
        //given
        Review review = fixtureFactory.make(
                ReviewBuilder.builder().withRegistrationStatus(RegistrationStatus.PENDING_REVIEW).build());
        RegistrationStatus status = RegistrationStatus.APPROVED;
        ReviewApprovedEvent expectedEvent = new ReviewApprovedEvent(review.getRootProblemId().getId());

        //when
        List<DomainEvent> events = reviewProcessor.update(review, status);

        //then
        assertThat(events).contains(expectedEvent);
    }

    @DisplayName("리뷰 업데이트 시 상태가 바뀌지않으면 이벤트를 생성하지않는다.")
    @Test
    void testUpdate2() {
        //given
        RegistrationStatus status = RegistrationStatus.PENDING_REVIEW;
        Review review = fixtureFactory.make(
                ReviewBuilder.builder().withRegistrationStatus(status).build());

        //when
        List<DomainEvent> events = reviewProcessor.update(review, status);

        //then
        assertThat(events).isEmpty();
    }
}
