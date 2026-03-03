package com.javis.learn_hub.evaluation.application;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.evaluation.application.dto.EvaluationCallbackRequest;
import com.javis.learn_hub.evaluation.domain.service.EvaluationProcessor;
import com.javis.learn_hub.evaluation.infrastructure.EvaluationClient;
import com.javis.learn_hub.evaluation.infrastructure.EvaluationRequestException;
import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.event.EvaluationFailedEvent;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class EvaluationService {

    private final EvaluationClient evaluationClient;

    private final EvaluationProcessor evaluationProcessor;
    private final AnswerProcessor answerProcessor;

    private final ProblemReader problemReader;
    private final InterviewReader interviewReader;
    private final AnswerReader answerReader;

    private final ApplicationEventPublisher eventPublisher;

    public void requestEvaluation(Long questionId) {
        Question question = interviewReader.getQuestion(questionId);
        Answer answer = answerReader.getByQuestionId(questionId);

        try {
            answerProcessor.prepareScoring(answer.getId());
        } catch (IllegalStateException | ObjectOptimisticLockingFailureException e) {
            log.info("이미 채점 진행 중, 중복 요청 무시: answerId={}", answer.getId());
            return;
        }

        ProblemScoringInfo scoringInfo = problemReader.getProblemScoringInfo(question.getProblemId().getId());
        evaluationClient.request(answer.getId(), scoringInfo.getReferenceAnswer(),
                scoringInfo.getKeywordsValue(), answer.getMessage());
        log.info("채점 요청 전송: answerId={}, questionId={}", answer.getId(), question.getId());
    }

    @Transactional
    public void completeEvaluation(EvaluationCallbackRequest request) {
        Answer answer = answerReader.get(request.answerId());

        try {
            answerProcessor.success(answer);
        } catch (IllegalStateException e) {
            log.warn("중복/늦은 콜백 무시: answerId={}, status={}", request.answerId(), answer.getEvaluationState());
            return;
        }

        Question question = interviewReader.getQuestion(answer.getQuestionId().getId());
        Long memberId = interviewReader.get(question.getInterviewId()).getMemberId().getId();

        List<DomainEvent> events = evaluationProcessor.complete(
                answer.getId(),
                answer.getQuestionId().getId(),
                memberId,
                request.grade(),
                request.feedback()
        );

        events.forEach(eventPublisher::publishEvent);
        log.info("채점 완료: answerId={}, grade={}", request.answerId(), request.grade());
    }
}
