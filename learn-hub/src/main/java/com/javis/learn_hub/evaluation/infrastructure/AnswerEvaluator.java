package com.javis.learn_hub.evaluation.infrastructure;

import com.javis.learn_hub.evaluation.domain.analysis.SegmentedSentence;
import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.List;

public interface AnswerEvaluator {
    EvaluationResponse evaluate(String question, String referenceAnswer, String userAnswer,
                                List<SegmentedSentence> sentences, ContentLanguage contentLanguage);
}
