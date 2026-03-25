package com.javis.learn_hub.interview.domain.service;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.member.domain.Role;
import com.javis.learn_hub.member.domain.repository.MemberRepository;
import com.javis.learn_hub.support.domain.Association;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InterviewStartPolicy {

    private static final long DAILY_INTERVIEW_LIMIT_PER_CATEGORY = 1L;

    private final MemberRepository memberRepository;
    private final InterviewRepository interviewRepository;

    public void validate(Long memberId, MainCategory mainCategory) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));

        if (member.getRole() == Role.ADMIN) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        long interviewCount = interviewRepository.countByMemberIdAndMainCategoryAndCreatedAtBetween(
                Association.from(memberId),
                mainCategory,
                startOfDay,
                startOfNextDay
        );

        if (interviewCount >= DAILY_INTERVIEW_LIMIT_PER_CATEGORY) {
            throw new IllegalStateException("오늘 " + toDisplayName(mainCategory) + " 인터뷰 사용 횟수를 모두 소진했습니다. 내일 다시 이용해주세요.");
        }
    }

    private String toDisplayName(MainCategory mainCategory) {
        return switch (mainCategory) {
            case COMPUTER_SCIENCE -> "컴퓨터 공학";
            case BACKEND -> "백엔드";
            case SYSTEM_DESIGN -> "시스템 설계";
            case CULTURE_FIT -> "컬처 핏";
        };
    }
}
