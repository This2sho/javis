package com.javis.learn_hub.answer.domain.repository;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.support.domain.Association;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface AnswerRepository extends Repository<Answer, Long> {

    Answer save(Answer answer);

    List<Answer> findAllByQuestionIdIn(List<Association<Question>> questionIds);
}
