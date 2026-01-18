package com.javis.learn_hub.review.domain;

/**
 *     PENDING_REVIEW: 문제 제출 후 관리자 리뷰 대기 상태
 *     APPROVED: 문제가 승인 완료
 *     REJECTED: 리뷰 완료되었으나 거절 됨
 */
public enum RegistrationStatus {
    PENDING_REVIEW,
    APPROVED,
    REJECTED;

    public static RegistrationStatus from(String registrationStatus) {
        return valueOf(registrationStatus.toUpperCase());
    }

    public boolean isApproved() {
        return this.equals(RegistrationStatus.APPROVED);
    }
}
