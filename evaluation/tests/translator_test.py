"""번역 모듈 테스트"""
from evaluation.core import translator


def test_contains_korean_true():
    """한글 포함 여부 - True"""
    assert translator.contains_korean("커넥션") is True
    assert translator.contains_korean("connection 커넥션") is True


def test_contains_korean_false():
    """한글 포함 여부 - False"""
    assert translator.contains_korean("connection") is False
    assert translator.contains_korean("TCP") is False


def test_translate_to_english_korean():
    """한글 단어 영어 번역"""
    result = translator.translate_to_english("연결")
    assert result is not None
    assert result != "연결"  # 번역됨


def test_translate_to_english_english():
    """영어 단어는 그대로 반환"""
    result = translator.translate_to_english("connection")
    assert result == "connection"


def test_translate_words_mixed():
    """혼합 단어 목록 번역"""
    words = ["커넥션", "connection", "연결"]
    result = translator.translate_words(words)

    assert "커넥션" in result
    assert "connection" in result
    assert "연결" in result
    assert result["connection"] == "connection"  # 영어는 그대로


def test_translate_words_cache():
    """번역 캐시 테스트"""
    # 같은 단어 두 번 번역
    translator.translate_to_english("신뢰성")
    translator.translate_to_english("신뢰성")

    # 캐시 히트 확인
    cache_info = translator.translate_to_english.cache_info()
    assert cache_info.hits >= 1
