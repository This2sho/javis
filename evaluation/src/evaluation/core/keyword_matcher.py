"""
키워드 매칭 모듈

사용자 답변에서 핵심 키워드 포함 여부 확인:
1. 원문 텍스트 직접 매칭
2. FastText 단어 유사도 기반 매칭 (한글→영어 번역 포함)
"""
import logging
import os
from pathlib import Path

import fasttext
import numpy as np

from evaluation.core import translator
from evaluation.core.model import word_extractor

logger = logging.getLogger(__name__)
BASE_DIR = Path(__file__).resolve().parents[2]
FASTTEXT_DIR = str(BASE_DIR) + "/evaluation/core/model/cc.en.50.bin"

FASTTEXT_MODEL_PATH = os.getenv(
    "FASTTEXT_MODEL_PATH",
    FASTTEXT_DIR
)
SIMILARITY_THRESHOLD = 0.65

# FastText 모델 (lazy loading)
_fasttext_model = None
_fasttext_available = None


def load_fasttext():
    """FastText 모델 로드 시도"""
    global _fasttext_model, _fasttext_available

    if _fasttext_available is not None:
        return _fasttext_available

    try:
        logger.info(f"[FastText] 모델 로드 시작: {FASTTEXT_MODEL_PATH}")
        _fasttext_model = fasttext.load_model(FASTTEXT_MODEL_PATH)
        _fasttext_available = True
        logger.info("[FastText] 모델 로드 완료")
    except Exception as e:
        logger.warning(f"[FastText] 모델 로드 실패: {e}")
        _fasttext_available = False

    return _fasttext_available


def _fasttext_similarity(word1: str, word2: str) -> float:
    """FastText로 두 단어 간 유사도 계산"""
    if not _fasttext_available or _fasttext_model is None:
        return 0.0
    try:
        v1 = _fasttext_model.get_word_vector(word1)
        v2 = _fasttext_model.get_word_vector(word2)
        return _calculate_cosine_similarity(v1, v2)
    except KeyError:
        return 0.0


def _calculate_cosine_similarity(vector1, vector2) -> float:
    return float(np.dot(vector1, vector2) / (np.linalg.norm(vector1) * np.linalg.norm(vector2)))


def find_similar_keywords(
    user_answer: str,
    keywords: list[str],
    threshold: float = SIMILARITY_THRESHOLD
) -> dict:
    """
    사용자 답변에서 키워드 포함 여부 확인

    Args:
        user_answer: 사용자 답변
        keywords: 핵심 키워드 목록
        threshold: 유사도 임계값

    Returns:
        dict: {keywords, included_keywords, missing_keywords}
    """
    # 1차: 원문 텍스트 직접 매칭
    included_1st, remaining = _match_by_raw_text(user_answer, keywords)

    if not remaining:
        return {
            "keywords": keywords,
            "included_keywords": included_1st,
            "missing_keywords": []
        }
    # 2차: FastText 유사도 기반 매칭
    load_fasttext()  # lazy load
    included_2nd, missing = _match_by_fasttext(user_answer, remaining, threshold)
    return {
        "keywords": keywords,
        "included_keywords": included_1st + included_2nd,
        "missing_keywords": missing
    }


def _match_by_raw_text(
    user_answer: str,
    keywords: list[str]
) -> tuple[list[str], list[str]]:
    """원문 텍스트 직접 매칭"""
    included = []
    remaining = []

    lower_answer = user_answer.lower()
    for keyword in keywords:
        if keyword.lower() in lower_answer:
            included.append(keyword)
        else:
            remaining.append(keyword)

    return included, remaining


def _match_by_fasttext(
    user_answer: str,
    keywords: list[str],
    threshold: float
) -> tuple[list[str], list[str]]:
    """FastText 유사도 기반 매칭 (한글→영어 번역 포함)"""
    included = []
    missing = []

    if not _fasttext_available:
        return [], keywords

    # 사용자 답변에서 의미있는 단어 추출
    user_words = word_extractor.extract_meaningful_words(user_answer)

    # 각 키워드별로 추출된 단어 매핑 (키워드 하나가 여러 단어로 분리될 수 있음)
    all_keyword_words = []
    keyword_to_words = {}

    for keyword in keywords:
        words = word_extractor.extract_meaningful_words_from_keywords([keyword])
        keyword_to_words[keyword] = words
        all_keyword_words.extend(words)

    # 한글 단어를 영어로 번역
    all_words = user_words + all_keyword_words
    translations = translator.translate_words(all_words)

    # 번역된 사용자 단어
    user_words_en = [translations.get(w, w) for w in user_words]
    logger.debug(f"[FastText] user_words: {list(zip(user_words, user_words_en))}")

    # 각 키워드별로 유사도 체크
    for keyword in keywords:
        keyword_words = keyword_to_words[keyword]
        keyword_words_en = [translations.get(w, w) for w in keyword_words]
        logger.debug(f"[FastText] keyword '{keyword}' words: {list(zip(keyword_words, keyword_words_en))}")

        found = False
        for keyword_word_en in keyword_words_en:
            for user_word_en in user_words_en:
                similarity = _fasttext_similarity(keyword_word_en, user_word_en)
                if similarity >= threshold:
                    found = True
                    break
            if found:
                break

        if found:
            included.append(keyword)
        else:
            missing.append(keyword)

    return included, missing
