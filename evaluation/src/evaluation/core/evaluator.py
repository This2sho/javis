"""
답변 평가기

사용자 답변을 채점하고 등급을 매기는 핵심 로직:
1. NLI 분류로 답변 관계 판단 (모순=0, 중립=0.25, 함의=0.5)
2. 키워드 포함 여부 확인 (포함률 * 0.5)
"""
import logging
import time

from evaluation.core import classification_model
from evaluation.core import keyword_matcher
from evaluation.core.evaluation_result import EvaluationResult
from evaluation.core.grade import Grade
from evaluation.core.keywords import Keywords
from evaluation.core.sentence import Sentence

logger = logging.getLogger(__name__)

INCORRECT_RESULT = EvaluationResult(Grade.INCORRECT, set())


def evaluate(user_answer: str, reference: dict) -> EvaluationResult:
    """
    사용자 답변 평가

    Args:
        user_answer: 사용자 답변 텍스트
        reference: {"answers": [...], "keywords": [...]}

    Returns:
        EvaluationResult: 평가 결과 (등급 + 피드백)
    """
    total_start = time.perf_counter()
    logger.info("[EVAL] 채점 시작")

    user_sentence = Sentence(user_answer)
    reference_answers = [Sentence(ans) for ans in reference["answers"]]
    keywords = Keywords(reference["keywords"])

    # 1. NLI 분류
    classify_start = time.perf_counter()
    nli_score, is_contradiction = _calculate_nli_score(user_sentence, reference_answers)
    classify_time = (time.perf_counter() - classify_start) * 1000
    logger.info(f"[EVAL] NLI 분류 완료: {classify_time:.2f}ms, score={nli_score:.2f}")

    # 모순이면 즉시 실패
    if is_contradiction:
        total_time = (time.perf_counter() - total_start) * 1000
        logger.info(f"[EVAL] 채점 완료 (INCORRECT - 모순): {total_time:.2f}ms")
        return INCORRECT_RESULT

    # 2. 키워드 점수 계산
    kw_start = time.perf_counter()
    keyword_score, missing_keywords = _calculate_keyword_score(user_sentence, keywords)
    kw_time = (time.perf_counter() - kw_start) * 1000
    logger.info(f"[EVAL] 키워드 매칭 완료: {kw_time:.2f}ms, score={keyword_score:.2f}")

    # 3. 최종 점수 및 등급
    final_score = nli_score + keyword_score
    grade = _determine_grade(final_score)

    total_time = (time.perf_counter() - total_start) * 1000
    logger.info(f"[EVAL] 채점 완료 ({grade.value}): 총 {total_time:.2f}ms, final_score={final_score:.2f}")

    return EvaluationResult(grade, missing_keywords)


def _calculate_nli_score(
    user_answer: Sentence,
    reference_answers: list[Sentence]
) -> tuple[float, bool]:
    """
    NLI 분류로 점수 계산

    Returns:
        tuple[float, bool]: (점수, 모순 여부)
        - 모순: 0점, True
        - 중립: 0.25점, False
        - 함의: 0.5점, False
    """
    best_score = 0.0

    for ref in reference_answers:
        label = classification_model.classify(user_answer, ref)

        # 모순이면 즉시 실패
        if label.is_contradiction():
            return 0.0, True

        # 최고 점수 갱신
        score = label.get_score()
        if score > best_score:
            best_score = score

    return best_score, False


def _calculate_keyword_score(
    user_answer: Sentence,
    keywords: Keywords
) -> tuple[float, set]:
    """
    키워드 포함률로 점수 계산

    Returns:
        tuple[float, set]: (점수 * 0.5, 누락 키워드)
    """
    result = keyword_matcher.find_similar_keywords(
        user_answer.value,
        keywords.value
    )

    included_count = len(result["included_keywords"])
    total_count = len(result["keywords"])

    # 키워드 포함률 * 0.5
    score = (included_count / total_count) * 0.5 if total_count > 0 else 0.0

    return score, set(result["missing_keywords"])


def _determine_grade(score: float) -> Grade:
    """점수에 따른 등급 결정"""
    if score >= 0.8:
        return Grade.PERFECT
    if score >= 0.65:
        return Grade.GOOD
    if score >= 0.5:
        return Grade.VAGUE
    return Grade.INCORRECT
