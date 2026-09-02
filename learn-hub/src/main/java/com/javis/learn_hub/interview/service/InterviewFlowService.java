package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.event.EvaluationRetryEvent;
import com.javis.learn_hub.event.NextQuestionReadyEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewProcessor;
import com.javis.learn_hub.interview.domain.service.InterviewStepFinder;
import com.javis.learn_hub.interview.domain.service.QuestionFlowProcessor;
import com.javis.learn_hub.interview.domain.service.dto.InterviewStepResult;
import com.javis.learn_hub.interview.domain.service.dto.NextQuestionResult;
import com.javis.learn_hub.interview.service.dto.InterviewerResponse;
import com.javis.learn_hub.interview.service.dto.QuestionResponse;
import com.javis.learn_hub.problem.domain.Difficulty;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class InterviewFlowService {

    private final InterviewProcessor interviewProcessor;
    private final InterviewStepFinder interviewStepFinder;
    private final QuestionFlowProcessor questionFlowProcessor;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public QuestionResponse start(String mainCategoryName, Long memberId, ContentLanguage contentLanguage) {
        MainCategory mainCategory = MainCategory.from(mainCategoryName);
        Optional<Interview> interview = interviewStepFinder.findActiveInterview(
                mainCategory,
                memberId,
                contentLanguage
        );
        if (interview.isPresent()) {
            return resumeInterview(interview.get());
        }
        List<Question> rootQuestions = interviewProcessor.initInterview(mainCategory, memberId, contentLanguage);
        return QuestionResponse.from(rootQuestions.get(0));
    }

    public QuestionResponse start(String mainCategoryName, Long memberId) {
        return start(mainCategoryName, memberId, ContentLanguage.KO);
    }

    private QuestionResponse resumeInterview(Interview interview) {
        InterviewStepResult step = interviewStepFinder.find(interview);
        if (step.needsRetryEvaluation()) {
            applicationEventPublisher.publishEvent(new EvaluationRetryEvent(step.questionId()));
        }
        if (step.needsNextQuestion()) {
            continueNextQuestion(step.questionId(), step.preferences());
        }
        return step.questionResponse();
    }

    @Transactional
    public void continueNextQuestion(Long questionId, List<Difficulty> preferences) {
        NextQuestionResult result = questionFlowProcessor.continueNextQuestion(questionId, preferences);
        Interview interview = result.interview();
        InterviewerResponse response = result.nextQuestion()
                .map(question -> InterviewerResponse.nextQuestion(interview.getId(), question.getId(), question.getMessage()))
                .orElseGet(() -> {
                    applicationEventPublisher.publishEvent(interviewProcessor.finish(interview));
                    return InterviewerResponse.finished(interview.getId());
                });
        applicationEventPublisher.publishEvent(new NextQuestionReadyEvent(interview.getMemberId().getId(), response));
    }

    @Transactional
    public void markQuestionAnswered(Long questionId) {
        questionFlowProcessor.markQuestionAnswered(questionId);
    }
}
