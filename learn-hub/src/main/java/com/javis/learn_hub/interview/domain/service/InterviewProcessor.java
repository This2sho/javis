package com.javis.learn_hub.interview.domain.service;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.event.InterviewFinishEvent;
import com.javis.learn_hub.interview.domain.EmptyProblemException;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.service.ProblemRecommender;
import com.javis.learn_hub.support.domain.Association;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InterviewProcessor {

    private static final int STARTING_PROBLEM_SIZE = 5;

    private final InterviewRepository interviewRepository;
    private final ProblemRecommender problemRecommender;
    private final QuestionFlowProcessor questionFlowProcessor;
    private final InterviewStartPolicy interviewStartPolicy;

    public List<Question> initInterview(MainCategory mainCategory, Long memberId, ContentLanguage contentLanguage) {
        interviewStartPolicy.validate(memberId, mainCategory, contentLanguage);
        List<Problem> rootProblems = problemRecommender.recommendRootProblems(memberId, mainCategory,
                contentLanguage, STARTING_PROBLEM_SIZE);
        if (rootProblems.isEmpty()) {
            throw new EmptyProblemException();
        }
        Interview interview = new Interview(Association.from(memberId), mainCategory, rootProblems.size(), contentLanguage);
        interviewRepository.save(interview);
        return questionFlowProcessor.createRootQuestions(rootProblems, interview.getId());
    }

    public List<Question> initInterview(MainCategory mainCategory, Long memberId) {
        return initInterview(mainCategory, memberId, ContentLanguage.KO);
    }

    public InterviewFinishEvent finish(Interview interview) {
        interview.finish();
        return new InterviewFinishEvent(interview.getId(), interview.getMemberId().getId());
    }
}
