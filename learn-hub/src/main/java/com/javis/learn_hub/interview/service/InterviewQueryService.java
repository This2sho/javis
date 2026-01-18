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
import java.util.List;
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
        return qnAs.stream()
                .map(QnAResponse::from)
                .toList();
    }
}
