"""
한글-영어 번역 모듈

한글 단어를 영어로 번역하여 FastText 유사도 비교 지원
- 메모리 효율: 외부 API 사용 (모델 로드 없음)
- 캐싱: 반복 번역 방지 (인메모리 + 파일 영속화)
"""
import json
import logging
import re
import threading
from functools import lru_cache
from pathlib import Path

from deep_translator import GoogleTranslator

logger = logging.getLogger(__name__)

KOREAN_PATTERN = re.compile(r'[가-힣]+')
CACHE_PATH = Path(__file__).resolve().parent / "translation_cache.json"

_translator = None
_file_cache: dict[str, str] = {}
_cache_lock = threading.Lock()


def _load_cache() -> None:
    """서버 시작 시 파일 캐시 로드"""
    global _file_cache
    if not CACHE_PATH.exists():
        return
    try:
        with open(CACHE_PATH, encoding="utf-8") as f:
            _file_cache = json.load(f)
        logger.info(f"[Translator] 캐시 로드: {len(_file_cache)}개 단어")
    except Exception as e:
        logger.warning(f"[Translator] 캐시 로드 실패: {e}")


def _save_cache() -> None:
    """파일 캐시 저장"""
    try:
        with _cache_lock:
            with open(CACHE_PATH, "w", encoding="utf-8") as f:
                json.dump(_file_cache, f, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.warning(f"[Translator] 캐시 저장 실패: {e}")


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
    한글 단어를 영어로 번역 (인메모리 캐시 → 파일 캐시 → API 순으로 조회)

    Args:
        word: 번역할 단어

    Returns:
        str: 번역된 영어 단어 (실패 시 원본 반환)
    """
    if not contains_korean(word):
        return word

    if word in _file_cache:
        return _file_cache[word]

    try:
        result = _get_translator().translate(word)
        translated = result.lower() if result else word
        logger.debug(f"[Translator] {word} -> {translated}")
        with _cache_lock:
            _file_cache[word] = translated
        _save_cache()
        return translated
    except Exception as e:
        logger.warning(f"[Translator] 번역 실패 ({word}): {e}")
        return word


def translate_words(words: list[str]) -> dict[str, str]:
    """
    여러 단어를 한번에 번역 (파일 캐시 히트 단어는 API 호출 제외)

    Args:
        words: 번역할 단어 목록

    Returns:
        dict: {원본: 번역} 매핑
    """
    result = {}
    uncached_korean = []

    for w in words:
        if not contains_korean(w):
            result[w] = w
        elif w in _file_cache:
            result[w] = _file_cache[w]
        else:
            uncached_korean.append(w)

    if not uncached_korean:
        return result

    # 미캐시 단어만 배치 번역
    try:
        batch_text = "\n".join(uncached_korean)
        translated = _get_translator().translate(batch_text)
        translated_list = translated.split("\n") if translated else uncached_korean

        new_entries = {}
        for orig, trans in zip(uncached_korean, translated_list):
            translated_word = trans.lower().strip() if trans else orig
            result[orig] = translated_word
            new_entries[orig] = translated_word

        with _cache_lock:
            _file_cache.update(new_entries)
        _save_cache()
        logger.debug(f"[Translator] 배치 번역 완료: {len(new_entries)}개 신규")
    except Exception as e:
        logger.warning(f"[Translator] 배치 번역 실패: {e}")
        for w in uncached_korean:
            result[w] = translate_to_english(w)

    return result


_load_cache()
