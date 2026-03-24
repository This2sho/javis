package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.event.InterviewFinishEvent;
import com.javis.learn_hub.event.NextQuestionReadyEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewProcessor;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.interview.service.dto.InterviewerResponse;
import com.javis.learn_hub.problem.domain.Difficulty;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NextQuestionService {

    private final InterviewProcessor interviewProcessor;
    private final InterviewReader interviewReader;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void continueNextQuestion(Long questionId, List<Difficulty> preferences) {
        Question previousQuestion = interviewReader.getQuestion(questionId);
        Interview interview = interviewReader.get(previousQuestion.getInterviewId());
        Optional<Question> nextQuestion = interviewProcessor
                .proceedToFollowUpQuestion(previousQuestion, preferences)
                .or(() -> interviewProcessor.proceedToNextRootQuestion(interview));
        InterviewerResponse response = nextQuestion
                .map(q -> InterviewerResponse.nextQuestion(interview.getId(), q.getId(), q.getMessage()))
                .orElseGet(() -> {
                    InterviewFinishEvent finishEvent = interviewProcessor.finish(interview);
                    applicationEventPublisher.publishEvent(finishEvent);
                    return InterviewerResponse.finished(interview.getId());
                });
        applicationEventPublisher.publishEvent(new NextQuestionReadyEvent(interview.getMemberId().getId(), response));
    }
}
