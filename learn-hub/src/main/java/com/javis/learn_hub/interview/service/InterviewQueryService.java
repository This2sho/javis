package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.answer.domain.service.AnswerFinder;
import com.javis.learn_hub.answer.domain.service.dto.QnA;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.interview.service.dto.InterviewHistoryDetailResponse;
import com.javis.learn_hub.interview.service.dto.InterviewHistoryResponse;
import com.javis.learn_hub.interview.service.dto.QnAResponse;
import com.javis.learn_hub.support.application.CursorPagingSupport;
import com.javis.learn_hub.support.application.dto.CursorPage;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorPageResponse;
import com.javis.learn_hub.support.domain.Association;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class InterviewQueryService {

    private final InterviewReader interviewReader;
    private final AnswerFinder answerFinder;

    public CursorPageResponse<InterviewHistoryResponse> viewHistories(
            CursorPageRequest cursorPageRequest,
            Long memberId) {
        List<Interview> interviews = interviewReader.getAllInterviews(memberId, cursorPageRequest);
        CursorPage<Interview> slicedInterviews = CursorPagingSupport.slice(interviews, cursorPageRequest);
        return collectToResponse(slicedInterviews);
    }

    private CursorPageResponse<InterviewHistoryResponse> collectToResponse(CursorPage<Interview> slicedInterviews) {
        List<InterviewHistoryResponse> responses = slicedInterviews.content()
                .stream()
                .map(interview -> InterviewHistoryResponse.from(interview))
                .toList();
        return new CursorPageResponse(responses, slicedInterviews.nextCursor(), slicedInterviews.hasNext());
    }

    public InterviewHistoryDetailResponse viewHistory(Long interviewId) {
        List<Question> questions = interviewReader.getAllQuestions(Association.from(interviewId));
        List<QnA> qnAs = answerFinder.findQnA(questions);
        return new InterviewHistoryDetailResponse(toQnAResponses(qnAs));
    }

    private List<QnAResponse> toQnAResponses(List<QnA> qnAs) {
        List<QnA> orderedQnAs = qnAs.stream()
                .sorted(Comparator
                        .comparing((QnA qna) -> qna.answer().getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(qna -> qna.answer().getId(), Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        Map<Long, Question> questionMap = orderedQnAs.stream()
                .map(QnA::question)
                .collect(java.util.stream.Collectors.toMap(Question::getId, question -> question));
        Map<Long, Integer> followUpSequenceByRootQuestionId = new HashMap<>();

        return orderedQnAs.stream()
                .map(qna -> QnAResponse.from(
                        qna,
                        buildDisplayOrder(qna.question(), questionMap, followUpSequenceByRootQuestionId)
                ))
                .toList();
    }

    private String buildDisplayOrder(
            Question question,
            Map<Long, Question> questionMap,
            Map<Long, Integer> followUpSequenceByRootQuestionId
    ) {
        Question rootQuestion = getRootQuestion(question, questionMap);
        int rootOrder = rootQuestion.getQuestionOrder() + 1;

        if (question.getParentQuestionId().isEmpty()) {
            return rootOrder + ".";
        }

        int followUpOrder = followUpSequenceByRootQuestionId.merge(rootQuestion.getId(), 1, Integer::sum);
        return rootOrder + "-" + followUpOrder + ".";
    }

    private Question getRootQuestion(Question question, Map<Long, Question> questionMap) {
        Question current = question;
        while (!current.getParentQuestionId().isEmpty()) {
            Question parent = questionMap.get(current.getParentQuestionId().getId());
            if (parent == null) {
                break;
            }
            current = parent;
        }
        return current;
    }
}
