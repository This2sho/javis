package com.javis.learn_hub.interview.domain.repository;

import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.QuestionStatus;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface QuestionRepository extends Repository<Question, Long> {

    void saveAll(Iterable<Question> questions);

    Question save(Question nextQuestion);

    Optional<Question> findById(Long id);

    List<Question> findAllByInterviewId(Association<Interview> interviewId);

    List<Question> findAllByInterviewIdAndQuestionStatus(Association<Interview> interviewId, QuestionStatus questionStatus);

    Optional<Question> findByInterviewIdAndParentQuestionIdAndQuestionOrder(Association<Interview> interviewId,
                                                                            Association<Question> parentQuestionId,
                                                                            int questionOrder);
}
