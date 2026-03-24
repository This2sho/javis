package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.evaluation.domain.service.EvaluationReader;
import com.javis.learn_hub.event.EvaluationRetryEvent;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.interview.domain.service.InterviewFinder;
import com.javis.learn_hub.interview.domain.service.InterviewProcessor;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
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
    private final AnswerReader answerReader;
    private final EvaluationReader evaluationReader;
    private final NextQuestionService nextQuestionService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public QuestionResponse start(String mainCategoryName, Long memberId) {
        MainCategory mainCategory = MainCategory.from(mainCategoryName);
        Optional<Interview> interview = interviewFinder.findActiveInterview(mainCategory, memberId);
        if (interview.isPresent()) {
            return resumeInterview(interview.get());
        }
        List<Question> rootQuestions = interviewProcessor.initInterview(mainCategory, memberId);
        return QuestionResponse.from(rootQuestions.get(0));
    }

    private QuestionResponse resumeInterview(Interview interview) {
        Question question = interviewReader.getCurrentQuestion(interview);
        if (question.getQuestionStatus() != QuestionStatus.ANSWERED) {
            return QuestionResponse.continueFrom(question);
        }
        return handleAnsweredQuestion(question);
    }

    private QuestionResponse handleAnsweredQuestion(Question question) {
        Answer answer = answerReader.getByQuestionId(question.getId());
        if (answer.needsEvaluation()) {
            applicationEventPublisher.publishEvent(new EvaluationRetryEvent(question.getId()));
            return QuestionResponse.pendingEvaluation(question);
        }
        nextQuestionService.continueNextQuestion(
                question.getId(),
                evaluationReader.getByAnswerId(answer.getId()).getPreferences()
        );
        return QuestionResponse.waitingForNextQuestion(question);
    }

    @Transactional
    public void continueNextQuestion(Long questionId, List<Difficulty> preferences) {
        nextQuestionService.continueNextQuestion(questionId, preferences);
    }

    @Transactional
    public void markQuestionAnswered(Long questionId) {
        Question question = interviewReader.getQuestion(questionId);
        interviewProcessor.markQuestionAnswered(question);
    }
}
