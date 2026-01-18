package com.javis.learn_hub.interview.domain.service;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.InterviewStatus;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.interview.domain.service.dto.ReferenceView;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.support.domain.Association;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InterviewFinder {

    private final InterviewRepository interviewRepository;
    private final InterviewReader interviewReader;
    private final ProblemReader problemReader;

    public Optional<Interview> findActiveInterview(MainCategory mainCategory, Long memberId) {
        return interviewRepository.findByMemberIdAndMainCategoryAndStatus(
                Association.from(memberId),
                mainCategory,
                InterviewStatus.ACTIVE
        );
    }

    public ReferenceView findReference(Long questionId) {
        Question question = interviewReader.getQuestion(questionId);
        Long problemId = question.getProblemId().getId();
        ProblemScoringInfo problemScoringInfo = problemReader.getProblemScoringInfo(problemId);
        return new ReferenceView(problemScoringInfo.getReferenceAnswer(),
                problemScoringInfo.getKeywords().getKeywords());
    }
}
