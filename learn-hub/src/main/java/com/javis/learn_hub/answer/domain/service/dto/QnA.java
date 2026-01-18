package com.javis.learn_hub.answer.domain.service.dto;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.interview.domain.Question;

public record QnA(Question question, Answer answer) {

}
