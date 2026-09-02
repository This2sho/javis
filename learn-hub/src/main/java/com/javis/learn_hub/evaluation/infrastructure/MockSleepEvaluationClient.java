package com.javis.learn_hub.evaluation.infrastructure;

import com.javis.learn_hub.evaluation.domain.analysis.SegmentedSentence;
import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import com.javis.learn_hub.support.i18n.ContentLanguage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Profile("test")
@Component
public class MockSleepEvaluationClient implements AnswerEvaluator {

    @Override
    public EvaluationResponse evaluate(String question,
                                       String referenceAnswer,
                                       String userAnswer,
                                       List<SegmentedSentence> sentences,
                                       ContentLanguage contentLanguage) {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("mock evaluator interrupted", e);
        }

        log.debug("mock evaluation completed after 1s sleep");
        return new EvaluationResponse(
                "부하 테스트용 mock evaluator",
                "GOOD",
                "부하 테스트용 1초 지연 응답입니다.",
                List.of(),
                List.of()
        );
    }
}
