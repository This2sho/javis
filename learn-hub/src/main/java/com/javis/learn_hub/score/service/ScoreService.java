package com.javis.learn_hub.score.service;

import com.javis.learn_hub.answer.domain.service.AnswerFinder;
import com.javis.learn_hub.answer.domain.service.dto.CategoryGrade;
import com.javis.learn_hub.answer.domain.service.dto.QnA;
import com.javis.learn_hub.category.domain.Category;
import com.javis.learn_hub.category.domain.CategoryScoreNode;
import com.javis.learn_hub.category.domain.MainCategory;
import com.javis.learn_hub.category.domain.service.CategoryReader;
import com.javis.learn_hub.evaluation.domain.analysis.SegmentedSentence;
import com.javis.learn_hub.evaluation.domain.analysis.SentenceAnnotation;
import com.javis.learn_hub.evaluation.domain.analysis.SentenceSegmenter;
import com.javis.learn_hub.interview.domain.Interview;
import com.javis.learn_hub.interview.domain.Question;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import com.javis.learn_hub.score.domain.Score;
import com.javis.learn_hub.score.domain.service.ScoreCalculator;
import com.javis.learn_hub.score.domain.service.ScoreProcessor;
import com.javis.learn_hub.score.domain.service.ScoreReader;
import com.javis.learn_hub.score.service.dto.CategoryScoreNodeResponse;
import com.javis.learn_hub.score.service.dto.CommonMistakeResponse;
import com.javis.learn_hub.score.service.dto.EnglishCoachingResponse;
import com.javis.learn_hub.score.service.dto.MainCategoryScoreResponse;
import com.javis.learn_hub.score.service.dto.MemorizeSentenceResponse;
import com.javis.learn_hub.score.service.dto.PracticeFocusResponse;
import com.javis.learn_hub.score.service.dto.ScoreDetailResponse;
import com.javis.learn_hub.score.service.dto.ScoreSummaryResponse;
import com.javis.learn_hub.support.domain.Association;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ScoreService {

    private final AnswerFinder answerFinder;
    private final ScoreReader scoreReader;
    private final ScoreProcessor scoreProcessor;
    private final ScoreCalculator scoreCalculator;
    private final CategoryReader categoryReader;
    private final InterviewReader interviewReader;
    private final ProblemReader problemReader;
    private final SentenceSegmenter sentenceSegmenter;

    private static final int PRACTICE_FOCUS_LIMIT = 5;
    private static final int ENGLISH_MEMORIZE_LIMIT = 6;
    private static final int COMMON_MISTAKE_LIMIT = 6;
    private static final int RECENT_ATTEMPT_WINDOW = 3;
    private static final long SLOW_RESPONSE_THRESHOLD_MS = 90_000L;

    public void applyScore(Long interviewId, Long memberId) {
        List<CategoryGrade> categoryGrades = answerFinder.findCategoryGrades(Association.from(interviewId));
        Set<Score> existingScores = scoreReader.getAllBy(Association.from(memberId), categoryGrades);
        List<Score> newScores = scoreProcessor.initNewScoresByZero(existingScores, categoryGrades, Association.from(memberId));
        existingScores.addAll(newScores);
        List<Score> calculatedScores = scoreCalculator.calculate(existingScores, categoryGrades);
        scoreProcessor.updateScores(calculatedScores);
    }

    public ScoreSummaryResponse showScores(Long memberId) {
        Association<Member> member = Association.<Member>from(memberId);
        List<MainCategoryScoreResponse> scores =
                Arrays.stream(MainCategory.values())
                        .map(mainCategory ->
                                new MainCategoryScoreResponse(
                                        mainCategory,
                                        scoreReader.getMainCategoryScore(member, mainCategory)
                                )
                        )
                        .toList();

        return new ScoreSummaryResponse(scores);
    }

    public ScoreDetailResponse showDetailScore(Long memberId, String mainCategoryName) {
        MainCategory mainCategory = MainCategory.from(mainCategoryName);
        Map<Category, Integer> allSubCategoryScores = scoreReader.getAllSubCategoryScores(
                Association.from(memberId), mainCategory
        );
        CategoryScoreNode categoryScoreNode = CategoryScoreNode.from(allSubCategoryScores);
        List<Category> leafCategories = categoryReader.getAllLeafSubCategoriesFrom(mainCategory);
        Map<Long, Integer> scoreByCategoryId = scoreReader.getScoresByLowest(Association.from(memberId), leafCategories)
                .stream()
                .collect(Collectors.toMap(score -> score.getCategoryId().getId(), Score::getScore));
        InterviewPracticeContext practiceContext = buildInterviewPracticeContext(memberId);
        return new ScoreDetailResponse(
                CategoryScoreNodeResponse.from(categoryScoreNode),
                buildPracticeFocuses(leafCategories, scoreByCategoryId, practiceContext),
                buildEnglishCoaching(memberId, leafCategories, practiceContext)
        );
    }

    private List<PracticeFocusResponse> buildPracticeFocuses(List<Category> leafCategories,
                                                             Map<Long, Integer> scoreByCategoryId,
                                                             InterviewPracticeContext practiceContext) {
        Map<Long, List<CategoryAttemptSnapshot>> attemptsByCategoryId = new HashMap<>();
        for (QnA qna : practiceContext.qnas()) {
            if (qna.evaluation() == null) {
                continue;
            }
            Problem problem = practiceContext.problemById().get(qna.question().getProblemId().getId());
            if (problem == null) {
                continue;
            }
            attemptsByCategoryId.computeIfAbsent(problem.getCategoryId().getId(), ignored -> new ArrayList<>())
                    .add(new CategoryAttemptSnapshot(
                            qna.answer().getCreatedAt(),
                            qna.evaluation().getScore(),
                            qna.answer().getResponseTimeMs(),
                            qna.evaluation().getFeedback()
                    ));
        }

        return leafCategories.stream()
                .filter(category -> attemptsByCategoryId.containsKey(category.getId()))
                .map(category -> toPracticeFocus(category, scoreByCategoryId.getOrDefault(category.getId(), 0),
                        attemptsByCategoryId.get(category.getId())))
                .sorted(Comparator
                        .comparingInt((PracticeFocusResponse response) -> practiceFocusPriority(response.reason()))
                        .thenComparingDouble(PracticeFocusResponse::recentAverageScore)
                        .thenComparingInt(PracticeFocusResponse::totalScore)
                        .thenComparing(PracticeFocusResponse::averageResponseTimeMs,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(PRACTICE_FOCUS_LIMIT)
                .toList();
    }

    private EnglishCoachingResponse buildEnglishCoaching(Long memberId,
                                                         List<Category> leafCategories,
                                                         InterviewPracticeContext practiceContext) {
        List<Association<Category>> leafCategoryIds = leafCategories.stream()
                .map(category -> Association.<Category>from(category.getId()))
                .toList();
        List<Problem> recommendableProblems = problemReader.getRecommendableRootProblems(leafCategoryIds, memberId);
        Map<Long, Category> categoryById = leafCategories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        Map<Long, ProblemScoringInfo> scoringInfoByProblemId = problemReader.getAllProblemScoringInfos(
                        recommendableProblems.stream()
                                .map(problem -> Association.<Problem>from(problem.getId()))
                                .toList()
                ).stream()
                .collect(Collectors.toMap(info -> info.getProblemId().getId(), Function.identity()));

        return new EnglishCoachingResponse(
                buildMemorizeSentences(recommendableProblems, scoringInfoByProblemId, categoryById),
                buildCommonMistakes(practiceContext.qnas(), practiceContext.problemById(), categoryById)
        );
    }

    private List<MemorizeSentenceResponse> buildMemorizeSentences(List<Problem> problems,
                                                                  Map<Long, ProblemScoringInfo> scoringInfoByProblemId,
                                                                  Map<Long, Category> categoryById) {
        Map<String, MemorizeSentenceCandidate> uniqueSentences = new LinkedHashMap<>();

        for (Problem problem : problems) {
            ProblemScoringInfo scoringInfo = scoringInfoByProblemId.get(problem.getId());
            if (scoringInfo == null || !isLikelyEnglish(scoringInfo.getReferenceAnswer())) {
                continue;
            }

            Category category = categoryById.get(problem.getCategoryId().getId());
            if (category == null) {
                continue;
            }

            for (SegmentedSentence sentence : sentenceSegmenter.segment(scoringInfo.getReferenceAnswer())) {
                if (!isGoodMemorizeSentence(sentence.text())) {
                    continue;
                }
                String key = normalizeSentenceKey(sentence.text());
                uniqueSentences.putIfAbsent(key, new MemorizeSentenceCandidate(sentence.text(), category.getPath()));
            }
        }

        return uniqueSentences.values().stream()
                .limit(ENGLISH_MEMORIZE_LIMIT)
                .map(candidate -> new MemorizeSentenceResponse(candidate.sentence(), candidate.categoryPath()))
                .toList();
    }

    private List<CommonMistakeResponse> buildCommonMistakes(List<QnA> qnas,
                                                            Map<Long, Problem> problemById,
                                                            Map<Long, Category> categoryById) {
        Map<String, CommonMistakeAccumulator> groupedMistakes = new LinkedHashMap<>();

        for (QnA qna : qnas) {
            if (qna.analysis() == null) {
                continue;
            }
            Problem problem = problemById.get(qna.question().getProblemId().getId());
            if (problem == null || !categoryById.containsKey(problem.getCategoryId().getId())) {
                continue;
            }

            Map<String, String> sentenceTextById = qna.analysis().sentences().stream()
                    .collect(Collectors.toMap(SegmentedSentence::sentenceId, SegmentedSentence::text));

            for (SentenceAnnotation annotation : qna.analysis().sentenceAnnotations()) {
                String originalSentence = sentenceTextById.get(annotation.sentenceId());
                if (originalSentence == null || !isLikelyEnglish(originalSentence)
                        || annotation.suggestion() == null || annotation.suggestion().isBlank()) {
                    continue;
                }

                String key = normalizeSentenceKey(originalSentence) + "->" + normalizeSentenceKey(annotation.suggestion());
                groupedMistakes.compute(key, (ignored, existing) -> {
                    if (existing == null) {
                        return new CommonMistakeAccumulator(
                                originalSentence,
                                annotation.suggestion().trim(),
                                firstNonBlank(annotation.reason(), annotation.issueType()),
                                1
                        );
                    }
                    return existing.increment(firstNonBlank(annotation.reason(), annotation.issueType()));
                });
            }
        }

        return groupedMistakes.values().stream()
                .sorted(Comparator.comparingLong(CommonMistakeAccumulator::count).reversed())
                .limit(COMMON_MISTAKE_LIMIT)
                .map(accumulator -> new CommonMistakeResponse(
                        accumulator.sentence(),
                        accumulator.suggestion(),
                        accumulator.reason(),
                        accumulator.count()
                ))
                .toList();
    }

    private InterviewPracticeContext buildInterviewPracticeContext(Long memberId) {
        List<Interview> endedInterviews = interviewReader.getAllEndedInterviews(memberId);
        List<Question> questions = endedInterviews.stream()
                .flatMap(interview -> interviewReader.getAllQuestions(Association.from(interview.getId())).stream())
                .toList();
        List<QnA> qnas = answerFinder.findQnA(questions);
        Map<Long, Problem> problemById = problemReader.getAll(
                        questions.stream().map(question -> question.getProblemId().getId()).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(Problem::getId, Function.identity()));
        return new InterviewPracticeContext(qnas, problemById);
    }

    private PracticeFocusResponse toPracticeFocus(Category category,
                                                  int totalScore,
                                                  List<CategoryAttemptSnapshot> snapshots) {
        List<CategoryAttemptSnapshot> recentSnapshots = snapshots.stream()
                .sorted(Comparator.comparing(CategoryAttemptSnapshot::createdAt).reversed())
                .limit(RECENT_ATTEMPT_WINDOW)
                .toList();

        double recentAverageScore = recentSnapshots.stream()
                .mapToInt(CategoryAttemptSnapshot::gradeScore)
                .average()
                .orElse(0.0);
        Long averageResponseTimeMs = recentSnapshots.stream()
                .map(CategoryAttemptSnapshot::responseTimeMs)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .average()
                .isPresent()
                ? Math.round(recentSnapshots.stream()
                        .map(CategoryAttemptSnapshot::responseTimeMs)
                        .filter(java.util.Objects::nonNull)
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0))
                : null;
        String latestFeedback = recentSnapshots.stream()
                .map(CategoryAttemptSnapshot::feedback)
                .filter(feedback -> feedback != null && !feedback.isBlank())
                .findFirst()
                .orElse(null);

        return new PracticeFocusResponse(
                category.getPath(),
                category.getMainCategory().getPath(),
                classifyReason(recentAverageScore, averageResponseTimeMs),
                totalScore,
                snapshots.size(),
                recentAverageScore,
                averageResponseTimeMs,
                latestFeedback
        );
    }

    private String classifyReason(double recentAverageScore, Long averageResponseTimeMs) {
        if (recentAverageScore <= 1.5) {
            return "LOW_RECENT_SCORE";
        }
        if (averageResponseTimeMs != null && averageResponseTimeMs >= SLOW_RESPONSE_THRESHOLD_MS) {
            return "SLOW_RESPONSE";
        }
        return "LOW_CUMULATIVE_SCORE";
    }

    private int practiceFocusPriority(String reason) {
        return switch (reason) {
            case "LOW_RECENT_SCORE" -> 0;
            case "SLOW_RESPONSE" -> 1;
            default -> 2;
        };
    }

    private boolean isLikelyEnglish(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        long latinCount = text.chars()
                .filter(ch -> (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))
                .count();
        long hangulCount = text.chars()
                .filter(ch -> ch >= 0xAC00 && ch <= 0xD7A3)
                .count();
        return latinCount >= 15 && latinCount >= hangulCount;
    }

    private boolean isGoodMemorizeSentence(String sentence) {
        if (!isLikelyEnglish(sentence)) {
            return false;
        }
        String trimmed = sentence == null ? "" : sentence.trim();
        return trimmed.length() >= 24
                && trimmed.length() <= 180
                && !trimmed.endsWith("?")
                && !trimmed.startsWith("-")
                && !trimmed.startsWith("*");
    }

    private String normalizeSentenceKey(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return Objects.requireNonNullElse(fallback, "");
    }

    private record CategoryAttemptSnapshot(
            java.time.LocalDateTime createdAt,
            int gradeScore,
            Long responseTimeMs,
            String feedback
    ) {
    }

    private record InterviewPracticeContext(
            List<QnA> qnas,
            Map<Long, Problem> problemById
    ) {
    }

    private record MemorizeSentenceCandidate(
            String sentence,
            String categoryPath
    ) {
    }

    private record CommonMistakeAccumulator(
            String sentence,
            String suggestion,
            String reason,
            long count
    ) {
        private CommonMistakeAccumulator increment(String nextReason) {
            return new CommonMistakeAccumulator(
                    sentence,
                    suggestion,
                    reason == null || reason.isBlank() ? nextReason : reason,
                    count + 1
            );
        }
    }
}
