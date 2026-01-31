package com.javis.learn_hub.interview.domain.service;

import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.InterviewStatus;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.interview.domain.repository.QuestionRepository;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InterviewReader {

    private final InterviewRepository interviewRepository;
    private final QuestionRepository questionRepository;

    public Interview get(Association<Interview> interviewId) {
        return get(interviewId.getId());
    }

    public Interview get(Long interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 인터뷰입니다."));
    }

    public Question getQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다."));
    }

    public List<Association<Problem>> getAllAnsweredProblemIds(Association<Interview> interviewId) {
        List<Question> answeredQuestions = questionRepository.findAllByInterviewIdAndQuestionStatusIn(
                interviewId,
                List.of(QuestionStatus.ANSWERED, QuestionStatus.COMPLETED));
        return answeredQuestions.stream()
                .map(Question::getProblemId)
                .toList();
    }

    public List<Question> getAllQuestions(Association<Interview> interviewId) {
        return questionRepository.findAllByInterviewId(interviewId);
    }

    public List<Interview> getAllInterviews(Long memberId, CursorPageRequest pageRequest) {
        Association<Member> member = Association.from(memberId);
        if (pageRequest.isDesc()) {
            return interviewRepository.findAllByInterviewStatusAndMemberIdByLatest(InterviewStatus.ENDED, pageRequest.getTargetTime(), pageRequest.getTargetId(),
                    member, pageRequest.getPageable());
        }
        return interviewRepository.findAllByInterviewStatusAndMemberIdByOldest(InterviewStatus.ENDED, pageRequest.getTargetTime(), pageRequest.getTargetId(),
                member, pageRequest.getPageable());
    }

    public Question getCurrentQuestion(Interview interview) {
        // ANSWERED 상태(답변은 됐지만 채점 안됨)인 질문이 있으면 우선 반환
        Optional<Question> pendingEvaluationQuestion = findPendingEvaluationQuestion(interview);
        if (pendingEvaluationQuestion.isPresent()) {
            return pendingEvaluationQuestion.get();
        }

        List<Question> unAnsweredQuestions = questionRepository.findAllByInterviewIdAndQuestionStatus(
                Association.from(interview.getId()),
                QuestionStatus.UNANSWERED
        );
        Optional<Question> followUpQuestion = unAnsweredQuestions.stream()
                .filter(Question::isFollowUpQuestion)
                .findAny();

        if (followUpQuestion.isPresent()) {
            return followUpQuestion.get();
        }

        return unAnsweredQuestions.stream()
                .filter(question -> question.getQuestionOrder() == interview.getCurrentQuestionOrder())
                .findAny()
                .orElseThrow(() -> new IllegalStateException("현재 인터뷰 상태가 잘못되었습니다."));
    }

    public Optional<Question> findPendingEvaluationQuestion(Interview interview) {
        List<Question> answeredQuestions = questionRepository.findAllByInterviewIdAndQuestionStatus(
                Association.from(interview.getId()),
                QuestionStatus.ANSWERED
        );
        return answeredQuestions.stream().findFirst();
    }
}
