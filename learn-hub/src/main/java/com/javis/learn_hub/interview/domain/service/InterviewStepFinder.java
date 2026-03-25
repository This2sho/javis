package com.javis.learn_hub.interview.domain.service;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.evaluation.domain.service.EvaluationReader;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.InterviewStatus;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.interview.domain.service.dto.InterviewStepResult;
import com.javis.learn_hub.support.domain.Association;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InterviewStepFinder {

    private final InterviewRepository interviewRepository;
    private final InterviewReader interviewReader;
    private final AnswerReader answerReader;
    private final EvaluationReader evaluationReader;

    public Optional<Interview> findActiveInterview(MainCategory mainCategory, Long memberId) {
        return interviewRepository.findByMemberIdAndMainCategoryAndStatus(
                Association.from(memberId),
                mainCategory,
                InterviewStatus.ACTIVE
        );
    }

    public InterviewStepResult find(Interview interview) {
        Question question = interviewReader.getCurrentQuestion(interview);
        if (question.isNotAnswered()) {
            return InterviewStepResult.continueCurrentQuestion(question);
        }

        Answer answer = answerReader.getByQuestionId(question.getId());
        if (answer.needsEvaluation()) {
            return InterviewStepResult.pendingEvaluation(question);
        }

        return InterviewStepResult.waitingForNextQuestion(
                question,
                evaluationReader.getByAnswerId(answer.getId()).getPreferences()
        );
    }
}
