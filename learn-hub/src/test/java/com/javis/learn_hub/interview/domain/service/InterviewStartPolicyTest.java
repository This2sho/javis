package com.javis.learn_hub.interview.domain.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.member.domain.Role;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.builder.InterviewBuilder;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterviewStartPolicyTest {

    private final TestFixtureFactory fixtureFactory = new TestFixtureFactory();
    private final InterviewStartPolicy interviewStartPolicy = new InterviewStartPolicy(
            fixtureFactory.getMemberRepository(),
            fixtureFactory.getInterviewRepository()
    );

    @DisplayName("일반 사용자는 같은 카테고리 인터뷰를 하루에 한 번만 시작할 수 있다.")
    @Test
    void testValidateWhenUserExceededDailyLimit() {
        Member member = fixtureFactory.make(MemberBuilder.builder().withSocialId(1L).build());
        fixtureFactory.make(InterviewBuilder.builder()
                .withMemberId(member.getId())
                .withMainCategory(MainCategory.COMPUTER_SCIENCE)
                .withContentLanguage(ContentLanguage.KO)
                .build());

        assertThatThrownBy(() -> interviewStartPolicy.validate(
                member.getId(),
                MainCategory.COMPUTER_SCIENCE,
                ContentLanguage.KO
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("오늘 컴퓨터 공학 인터뷰 사용 횟수를 모두 소진했습니다. 내일 다시 이용해주세요.");
    }

    @DisplayName("관리자는 같은 카테고리 인터뷰를 하루에 여러 번 시작할 수 있다.")
    @Test
    void testValidateWhenAdminExceededDailyLimit() {
        Member admin = fixtureFactory.make(MemberBuilder.builder()
                .withSocialId(2L)
                .withRole(Role.ADMIN)
                .build());
        Interview interview = fixtureFactory.make(InterviewBuilder.builder()
                .withMemberId(admin.getId())
                .withMainCategory(MainCategory.COMPUTER_SCIENCE)
                .build());

        assertThatCode(() -> interviewStartPolicy.validate(
                admin.getId(),
                interview.getMainCategory(),
                ContentLanguage.KO
        ))
                .doesNotThrowAnyException();
    }

    @DisplayName("일반 사용자는 다른 카테고리 인터뷰는 같은 날에도 시작할 수 있다.")
    @Test
    void testValidateWhenUserStartsDifferentCategory() {
        Member member = fixtureFactory.make(MemberBuilder.builder().withSocialId(3L).build());
        fixtureFactory.make(InterviewBuilder.builder()
                .withMemberId(member.getId())
                .withMainCategory(MainCategory.COMPUTER_SCIENCE)
                .withContentLanguage(ContentLanguage.KO)
                .build());

        assertThatCode(() -> interviewStartPolicy.validate(
                member.getId(),
                MainCategory.SYSTEM_DESIGN,
                ContentLanguage.KO
        ))
                .doesNotThrowAnyException();
    }

    @DisplayName("일반 사용자는 같은 카테고리라도 언어가 다르면 같은 날 새 인터뷰를 시작할 수 있다.")
    @Test
    void testValidateWhenUserStartsSameCategoryDifferentLanguage() {
        Member member = fixtureFactory.make(MemberBuilder.builder().withSocialId(4L).build());
        fixtureFactory.make(InterviewBuilder.builder()
                .withMemberId(member.getId())
                .withMainCategory(MainCategory.COMPUTER_SCIENCE)
                .withContentLanguage(ContentLanguage.EN)
                .build());

        assertThatCode(() -> interviewStartPolicy.validate(
                member.getId(),
                MainCategory.COMPUTER_SCIENCE,
                ContentLanguage.KO
        )).doesNotThrowAnyException();
    }
}
