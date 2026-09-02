package com.javis.learn_hub.evaluation.domain.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SentenceSegmenterTest {

    private final SentenceSegmenter sentenceSegmenter = new SentenceSegmenter();

    @Test
    void splitsEnglishAnswerIntoStableSentenceIds() {
        List<SegmentedSentence> sentences = sentenceSegmenter.segment("""
                One of the projects I'm most proud of is an AI interview preparation service.
                The biggest challenge was improving the grading process.
                It works well when requests are small.
                """);

        assertThat(sentences).extracting(SegmentedSentence::sentenceId)
                .containsExactly("S1", "S2", "S3");
        assertThat(sentences).extracting(SegmentedSentence::text)
                .containsExactly(
                        "One of the projects I'm most proud of is an AI interview preparation service.",
                        "The biggest challenge was improving the grading process.",
                        "It works well when requests are small."
                );
    }

    @Test
    void doesNotSplitOnCommonAbbreviationsOrDecimalPoints() {
        List<SegmentedSentence> sentences = sentenceSegmenter.segment("""
                I improved latency from 3.5 seconds to 1.2 seconds.
                I read docs, e.g. JVM tuning guides, before applying changes.
                """);

        assertThat(sentences).hasSize(2);
        assertThat(sentences.get(0).text()).isEqualTo("I improved latency from 3.5 seconds to 1.2 seconds.");
        assertThat(sentences.get(1).text()).isEqualTo("I read docs, e.g. JVM tuning guides, before applying changes.");
    }
}
