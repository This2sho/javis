package com.javis.learn_hub.evaluation.domain.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SentenceSegmenter {

    private static final Set<String> COMMON_ABBREVIATIONS = Set.of(
            "e.g.", "i.e.", "etc.", "mr.", "mrs.", "ms.", "dr.", "prof.", "u.s.", "a.i."
    );

    public List<SegmentedSentence> segment(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        List<SegmentedSentence> sentences = new ArrayList<>();

        int start = 0;
        int sentenceNumber = 1;
        int index = 0;
        while (index < normalized.length()) {
            char current = normalized.charAt(index);
            if (current == '\n' && shouldSplitOnLineBreak(normalized, start, index)) {
                sentenceNumber = appendSentence(sentences, normalized, start, index, sentenceNumber);
                start = index + 1;
            } else if (isSentenceEndingPunctuation(current) && isSentenceBoundary(normalized, index)) {
                sentenceNumber = appendSentence(sentences, normalized, start, index + 1, sentenceNumber);
                start = index + 1;
            }
            index++;
        }

        appendSentence(sentences, normalized, start, normalized.length(), sentenceNumber);
        return mergeTinyFragments(sentences);
    }

    private boolean shouldSplitOnLineBreak(String text, int start, int newlineIndex) {
        String chunk = text.substring(start, newlineIndex).trim();
        if (chunk.isEmpty()) {
            return true;
        }
        return chunk.length() >= 20 || endsWithSentenceLikeEnding(chunk);
    }

    private boolean endsWithSentenceLikeEnding(String text) {
        return text.endsWith(".")
                || text.endsWith("?")
                || text.endsWith("!")
                || text.endsWith("다")
                || text.endsWith("요")
                || text.endsWith("죠")
                || text.endsWith("니다");
    }

    private boolean isSentenceEndingPunctuation(char current) {
        return current == '.' || current == '?' || current == '!';
    }

    private boolean isSentenceBoundary(String text, int punctuationIndex) {
        if (text.charAt(punctuationIndex) == '.' && isDecimalPoint(text, punctuationIndex)) {
            return false;
        }
        if (text.charAt(punctuationIndex) == '.' && isAbbreviation(text, punctuationIndex)) {
            return false;
        }

        int next = punctuationIndex + 1;
        while (next < text.length() && isClosingDecoration(text.charAt(next))) {
            next++;
        }
        return next >= text.length() || Character.isWhitespace(text.charAt(next));
    }

    private boolean isDecimalPoint(String text, int punctuationIndex) {
        return punctuationIndex > 0
                && punctuationIndex < text.length() - 1
                && Character.isDigit(text.charAt(punctuationIndex - 1))
                && Character.isDigit(text.charAt(punctuationIndex + 1));
    }

    private boolean isAbbreviation(String text, int punctuationIndex) {
        int start = punctuationIndex;
        while (start > 0 && !Character.isWhitespace(text.charAt(start - 1)) && text.charAt(start - 1) != '\n') {
            start--;
        }
        String token = text.substring(start, punctuationIndex + 1).toLowerCase(Locale.ROOT);
        return COMMON_ABBREVIATIONS.contains(token) || token.matches("([a-z]\\.){2,}");
    }

    private boolean isClosingDecoration(char current) {
        return current == '"' || current == '\'' || current == ')' || current == ']' || current == '”';
    }

    private int appendSentence(List<SegmentedSentence> sentences,
                               String text,
                               int startInclusive,
                               int endExclusive,
                               int sentenceNumber) {
        if (startInclusive >= endExclusive) {
            return sentenceNumber;
        }

        int trimmedStart = startInclusive;
        while (trimmedStart < endExclusive && Character.isWhitespace(text.charAt(trimmedStart))) {
            trimmedStart++;
        }

        int trimmedEnd = endExclusive;
        while (trimmedEnd > trimmedStart && Character.isWhitespace(text.charAt(trimmedEnd - 1))) {
            trimmedEnd--;
        }

        if (trimmedStart >= trimmedEnd) {
            return sentenceNumber;
        }

        sentences.add(new SegmentedSentence(
                "S" + sentenceNumber,
                text.substring(trimmedStart, trimmedEnd),
                trimmedStart,
                trimmedEnd
        ));
        return sentenceNumber + 1;
    }

    private List<SegmentedSentence> mergeTinyFragments(List<SegmentedSentence> original) {
        if (original.isEmpty()) {
            return original;
        }

        List<SegmentedSentence> merged = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        int bufferStart = -1;
        int bufferEnd = -1;

        for (SegmentedSentence sentence : original) {
            if (sentence.text().length() <= 6 && !merged.isEmpty()) {
                SegmentedSentence previous = merged.remove(merged.size() - 1);
                merged.add(new SegmentedSentence(
                        previous.sentenceId(),
                        previous.text() + " " + sentence.text(),
                        previous.startIndex(),
                        sentence.endIndex()
                ));
                continue;
            }

            if (buffer.length() > 0) {
                merged.add(new SegmentedSentence(
                        "S" + (merged.size() + 1),
                        buffer.toString().trim(),
                        bufferStart,
                        bufferEnd
                ));
                buffer.setLength(0);
            }

            buffer.append(sentence.text());
            bufferStart = sentence.startIndex();
            bufferEnd = sentence.endIndex();
        }

        if (buffer.length() > 0) {
            merged.add(new SegmentedSentence(
                    "S" + (merged.size() + 1),
                    buffer.toString().trim(),
                    bufferStart,
                    bufferEnd
            ));
        }
        return merged;
    }
}
