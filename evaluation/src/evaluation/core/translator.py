"""
한글-영어 번역 모듈

한글 단어를 영어로 번역하여 FastText 유사도 비교 지원
- 메모리 효율: 외부 API 사용 (모델 로드 없음)
- 캐싱: 반복 번역 방지
"""
import logging
import re
from functools import lru_cache

from deep_translator import GoogleTranslator

logger = logging.getLogger(__name__)

KOREAN_PATTERN = re.compile(r'[가-힣]+')

_translator = None


def _get_translator() -> GoogleTranslator:
    """번역기 싱글톤 반환"""
    global _translator
    if _translator is None:
        _translator = GoogleTranslator(source='ko', target='en')
    return _translator


def contains_korean(text: str) -> bool:
    """한글 포함 여부 확인"""
    return bool(KOREAN_PATTERN.search(text))


@lru_cache(maxsize=1024)
def translate_to_english(word: str) -> str:
    """
    한글 단어를 영어로 번역 (캐싱 적용)

    Args:
        word: 번역할 단어

    Returns:
        str: 번역된 영어 단어 (실패 시 원본 반환)
    """
    if not contains_korean(word):
        return word

    try:
        result = _get_translator().translate(word)
        logger.debug(f"[Translator] {word} -> {result}")
        return result.lower() if result else word
    except Exception as e:
        logger.warning(f"[Translator] 번역 실패 ({word}): {e}")
        return word


def translate_words(words: list[str]) -> dict[str, str]:
    """
    여러 단어를 한번에 번역

    Args:
        words: 번역할 단어 목록

    Returns:
        dict: {원본: 번역} 매핑
    """
    result = {}
    korean_words = [w for w in words if contains_korean(w)]

    if not korean_words:
        return {w: w for w in words}

    # 배치 번역 (줄바꿈으로 구분)
    try:
        batch_text = "\n".join(korean_words)
        translated = _get_translator().translate(batch_text)
        translated_list = translated.split("\n") if translated else korean_words

        for orig, trans in zip(korean_words, translated_list):
            result[orig] = trans.lower().strip() if trans else orig
            # 캐시에도 저장
            translate_to_english.cache_info()  # 캐시 활성화 확인용
    except Exception as e:
        logger.warning(f"[Translator] 배치 번역 실패: {e}")
        # 실패 시 개별 번역 시도
        for w in korean_words:
            result[w] = translate_to_english(w)

    # 영어 단어는 그대로
    for w in words:
        if w not in result:
            result[w] = w

    return result
