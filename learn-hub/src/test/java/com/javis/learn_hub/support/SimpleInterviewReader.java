package com.javis.learn_hub.support;

import com.javis.learn_hub.interview.domain.repository.InterviewRepository;
import com.javis.learn_hub.interview.domain.repository.QuestionRepository;
import com.javis.learn_hub.interview.domain.service.InterviewReader;

public class SimpleInterviewReader extends InterviewReader {

    public SimpleInterviewReader(InterviewRepository interviewRepository,
                                 QuestionRepository questionRepository) {
        super(interviewRepository, questionRepository);
    }
}
