package com.javis.learn_hub.interview.domain.service;

import com.javis.learn_hub.answer.domain.service.AnswerReader;
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
    private final AnswerReader answerReader;

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
        return questionRepository.findAllByInterviewIdAndQuestionStatus(interviewId, QuestionStatus.ANSWERED)
                .stream()
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
        return findPendingEvaluationQuestion(interview)
                .or(() -> findFollowUpQuestion(interview))
                .or(() -> findCurrentRootQuestion(interview))
                .or(() -> findAnsweredRootQuestionAtCurrentOrder(interview))
                .orElseThrow(() -> new IllegalStateException("현재 인터뷰 상태가 잘못되었습니다."));
    }

    public Long getMemberIdByQuestionId(Long questionId) {
        Question question = getQuestion(questionId);
        return get(question.getInterviewId()).getMemberId().getId();
    }

    public Optional<Question> findPendingEvaluationQuestion(Interview interview) {
        return questionRepository.findAllByInterviewIdAndQuestionStatus(
                        Association.from(interview.getId()), QuestionStatus.ANSWERED)
                .stream()
                .filter(q -> answerReader.getByQuestionId(q.getId()).isPendingEvaluation())
                .findFirst();
    }

    private Optional<Question> findFollowUpQuestion(Interview interview) {
        return getUnansweredQuestions(interview).stream()
                .filter(Question::isFollowUpQuestion)
                .findAny();
    }

    private Optional<Question> findCurrentRootQuestion(Interview interview) {
        return getUnansweredQuestions(interview).stream()
                .filter(question -> question.getQuestionOrder() == interview.getCurrentQuestionOrder())
                .findAny();
    }

    private Optional<Question> findAnsweredRootQuestionAtCurrentOrder(Interview interview) {
        return questionRepository.findByInterviewIdAndParentQuestionIdAndQuestionOrder(
                Association.from(interview.getId()),
                Association.getEmpty(),
                interview.getCurrentQuestionOrder()
        );
    }

    private List<Question> getUnansweredQuestions(Interview interview) {
        return questionRepository.findAllByInterviewIdAndQuestionStatus(
                Association.from(interview.getId()),
                QuestionStatus.UNANSWERED
        );
    }
}
