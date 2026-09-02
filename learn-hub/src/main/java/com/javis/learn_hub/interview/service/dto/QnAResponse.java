package com.javis.learn_hub.interview.service.dto;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.domain.service.dto.QnA;
import com.javis.learn_hub.evaluation.domain.Evaluation;
import com.javis.learn_hub.evaluation.domain.analysis.MissingPoint;
import com.javis.learn_hub.evaluation.domain.analysis.SegmentedSentence;
import com.javis.learn_hub.evaluation.domain.analysis.SentenceAnnotation;
import com.javis.learn_hub.interview.domain.Question;
import java.util.List;

public record QnAResponse(
        String displayOrder,
        String question,
        String answer,
        String feedBack,
        Long responseTimeMs,
        List<SegmentedSentence> sentences,
        List<SentenceAnnotation> sentenceAnnotations,
        List<MissingPoint> missingPoints
) {

    public static QnAResponse from(QnA qnA, String displayOrder) {
        Question question = qnA.question();
        Answer answer = qnA.answer();
        Evaluation evaluation = qnA.evaluation();
        String feedback = evaluation != null ? evaluation.getFeedback() : null;
        return new QnAResponse(
                displayOrder,
                question.getMessage(),
                answer.getMessage(),
                feedback,
                answer.getResponseTimeMs(),
                qnA.analysis().sentences(),
                qnA.analysis().sentenceAnnotations(),
                qnA.analysis().missingPoints()
        );
    }
}
