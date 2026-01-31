package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.event.EvaluationRetryEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewFinder;
import com.javis.learn_hub.interview.domain.service.InterviewProcessor;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.interview.service.dto.InterviewerResponse;
import com.javis.learn_hub.interview.service.dto.QuestionResponse;
import com.javis.learn_hub.problem.domain.Difficulty;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class InterviewCommandService {

    private final InterviewProcessor interviewProcessor;
    private final InterviewReader interviewReader;
    private final InterviewFinder interviewFinder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public QuestionResponse start(String mainCategoryName, Long memberId) {
        MainCategory mainCategory = MainCategory.from(mainCategoryName);
        Optional<Interview> interview = interviewFinder.findActiveInterview(mainCategory, memberId);
        if (interview.isPresent()) {
            Question question = interviewReader.getCurrentQuestion(interview.get());
            if (question.isPendingEvaluation()) {
                applicationEventPublisher.publishEvent(new EvaluationRetryEvent(question.getId()));
                return QuestionResponse.pendingEvaluation(question);
            }
            return QuestionResponse.continueFrom(question);
        }
        List<Question> rootQuestions = interviewProcessor.initInterview(mainCategory, memberId);
        return QuestionResponse.from(rootQuestions.get(0));
    }

    @Transactional
    public InterviewerResponse continueNextQuestion(Long questionId, List<Difficulty> preferences) {
        Question previousQuestion = interviewReader.getQuestion(questionId);
        Interview interview = interviewReader.get(previousQuestion.getInterviewId());
        Optional<Question> nextQuestion = interviewProcessor
                .proceedToFollowUpQuestion(previousQuestion, preferences)
                .or(() -> interviewProcessor.proceedToNextRootQuestion(interview));
        if (nextQuestion.isPresent()) {
            return new InterviewerResponse(false, interview.getId(), nextQuestion.get().getId(), nextQuestion.get().getMessage());
        }
        List<DomainEvent> events = interviewProcessor.finish(interview);
        events.forEach(applicationEventPublisher::publishEvent);
        return InterviewerResponse.finished(interview.getId());
    }

    @Transactional
    public void markQuestionAnswered(Long questionId) {
        Question question = interviewReader.getQuestion(questionId);
        interviewProcessor.markQuestionAnswered(question);
    }

    @Transactional
    public void markQuestionCompleted(Long questionId) {
        Question question = interviewReader.getQuestion(questionId);
        interviewProcessor.markQuestionCompleted(question);
    }
}
