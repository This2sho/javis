package com.javis.learn_hub.support.builder;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.support.domain.Association;

public class InterviewBuilder {

    private Long memberId = 1L;
    private int totalQuestions = 3;
    private MainCategory mainCategory = MainCategory.COMPUTER_SCIENCE;

    public static InterviewBuilder builder() {
        return new InterviewBuilder();
    }

    public InterviewBuilder withMemberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public InterviewBuilder withMainCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
        return this;
    }

    public InterviewBuilder withTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
        return this;
    }

    public Interview build() {
        return new Interview(Association.from(memberId), mainCategory, totalQuestions);
    }
}
