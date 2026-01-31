package com.javis.learn_hub.support.repository;

import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.interview.domain.repository.QuestionRepository;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Optional;

public class InMemoryQuestionRepository extends InMemoryRepository<Question> implements QuestionRepository {

    @Override
    public List<Question> findAllByInterviewId(Association<Interview> interviewId) {
        return findAll(q -> q.getInterviewId().equals(interviewId));
    }

    @Override
    public List<Question> findAllByInterviewIdAndQuestionStatus(Association<Interview> interviewId,
                                                                QuestionStatus questionStatus) {
        return findAll(q -> q.getInterviewId().equals(interviewId) && q.getQuestionStatus().equals(questionStatus));
    }

    @Override
    public Optional<Question> findByInterviewIdAndParentQuestionIdAndQuestionOrder(Association<Interview> interviewId, Association<Question> parentQuestionId, int questionOrder) {
        return findOne(q ->
                q.getInterviewId().equals(interviewId)
                        && q.getParentQuestionId().equals(parentQuestionId)
                        && q.getQuestionOrder() == questionOrder);
    }

    @Override
    public List<Question> findAllByInterviewIdAndQuestionStatusIn(Association<Interview> interviewId, List<QuestionStatus> questionStatuses) {
        return findAll(q -> q.getInterviewId().equals(interviewId) && questionStatuses.contains(q.getQuestionStatus()));
    }
}
