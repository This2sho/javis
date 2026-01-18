package com.javis.learn_hub.support;

import com.javis.learn_hub.answer.domain.repository.AnswerRepository;
import com.javis.learn_hub.answer.domain.service.AnswerReader;

public class SimpleAnswerReader extends AnswerReader {

    public SimpleAnswerReader(AnswerRepository answerRepository) {
        super(answerRepository);
    }
}
