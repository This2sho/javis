package com.javis.learn_hub.interview.domain.service;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.InterviewStatus;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.support.domain.Association;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InterviewFinder {

    private final InterviewRepository interviewRepository;

    public Optional<Interview> findActiveInterview(MainCategory mainCategory, Long memberId) {
        return interviewRepository.findByMemberIdAndMainCategoryAndStatus(
                Association.from(memberId),
                mainCategory,
                InterviewStatus.ACTIVE
        );
    }
}
