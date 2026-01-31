package com.javis.learn_hub.evaluation.application;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.AnswerReader;
import com.javis.learn_hub.evaluation.application.dto.EvaluationCallbackRequest;
import com.javis.learn_hub.evaluation.domain.service.EvaluationProcessor;
import com.javis.learn_hub.evaluation.infrastructure.EvaluationClient;
import com.javis.learn_hub.event.DomainEvent;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class EvaluationService {

    private final EvaluationProcessor evaluationProcessor;
    private final EvaluationClient evaluationClient;

    private final ProblemReader problemReader;
    private final InterviewReader interviewReader;
    private final AnswerReader answerReader;

    private final ApplicationEventPublisher eventPublisher;

    public void requestEvaluation(Long questionId) {
        Question question = interviewReader.getQuestion(questionId);
        Answer answer = answerReader.getByQuestionId(questionId);
        Long problemId = question.getProblemId().getId();
        ProblemScoringInfo scoringInfo = problemReader.getProblemScoringInfo(problemId);

        evaluationClient.requestAsync(
                answer.getId(),
                scoringInfo.getReferenceAnswer(),
                scoringInfo.getKeywordsValue(),
                answer.getMessage()
        );

        log.info("채점 요청 전송 완료: answerId={}, questionId={}", answer.getId(), question.getId());
    }

    @Transactional
    public void completeEvaluation(EvaluationCallbackRequest request) {
        Answer answer = answerReader.get(request.answerId());
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
