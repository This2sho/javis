from evaluation.core import keyword_matcher
from evaluation.core.evaluator import evaluate
from evaluation.core.grade import Grade


def test_evaluate_perfect():
    user_answer = "tcp는 연결 중심으로 신뢰성을 보장한다."
    reference = {"answers": ["tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이다."], "keywords": ["TCP", "연결", "신뢰성", "보장"]}
    result = evaluate(user_answer, reference)
    assert result.grade == Grade.PERFECT


def test_evaluate_good():
    user_answer = "tcp는 연결 중심의 프로토콜이다."
    reference = {"answers": ["tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이다."], "keywords": ["TCP", "연결", "신뢰성", "보장"]}
    result = evaluate(user_answer, reference)
    assert result.grade == Grade.GOOD


def test_evaluate_vague():
    user_answer = "tcp는 프로토콜이다."
    reference = {"answers": ["tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이다."], "keywords": ["TCP", "연결", "신뢰성", "보장"]}
    result = evaluate(user_answer, reference)
    assert result.grade == Grade.VAGUE


def test_evaluate_incorrect():
    user_answer = "tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이 아니다."
    reference = {"answers": ["tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이다."], "keywords": ["TCP", "연결", "신뢰성", "보장"]}
    result = evaluate(user_answer, reference)
    assert result.grade == Grade.INCORRECT


def test_evaluate_incorrect2():
    user_answer = "tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이 아니다."
    reference = {"answers": ["tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이다."], "keywords": ["TCP", "연결", "신뢰성", "보장"]}
    result = evaluate(user_answer, reference)
    assert result.grade == Grade.INCORRECT


def test_evaluate_incorrect3():
    user_answer = "tcp는 비연결 중심의 신뢰성을 보장하지않는 프로토콜이다."
    reference = {"answers": ["tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이다."], "keywords": ["TCP", "연결", "신뢰성", "보장"]}
    result = evaluate(user_answer, reference)
    assert result.grade == Grade.INCORRECT


def test_low_keyword_score():
    """커넥션 = 연결 유사어 매칭 테스트"""
    user_answer = "커넥션 타임아웃 설정은 잘해야한다."
    keywords = ["TCP", "연결", "신뢰성", "보장"]
    result = keyword_matcher.find_similar_keywords(user_answer, keywords)
    score = len(result["included_keywords"]) / len(result["keywords"])
    expected_score = 1/4  # 커넥션 = 연결
    assert score == expected_score


def test_low_keyword_score2():
    """connection = 커넥션 유사어 매칭 테스트"""
    user_answer = "connection이 있다"
    keywords = ["TCP", "커넥션", "신뢰성", "보장"]
    result = keyword_matcher.find_similar_keywords(user_answer, keywords)
    print(result["included_keywords"])
    print(result["missing_keywords"])
    score = len(result["included_keywords"]) / len(result["keywords"])
    expected_score = 1/4
    assert score == expected_score


def test_mid_keyword_score():
    """직접 텍스트 매칭 테스트"""
    user_answer = "TCP는 연결이다."
    keywords = ["TCP", "연결", "신뢰성", "보장"]
    result = keyword_matcher.find_similar_keywords(user_answer, keywords)
    score = len(result["included_keywords"]) / len(result["keywords"])
    expected_score = 2/4  # TCP, 연결
    assert score == expected_score


def test_high_keyword_score():
    """3/4 키워드 매칭 테스트"""
    user_answer = "TCP는 연결 지향으로 신뢰성을 가진다."
    keywords = ["TCP", "연결", "신뢰성", "보장"]
    result = keyword_matcher.find_similar_keywords(user_answer, keywords)
    score = len(result["included_keywords"]) / len(result["keywords"])
    expected_score = 3/4  # TCP, 연결, 신뢰성
    assert score == expected_score


def test_korean_to_english_translation_matching():
    """한글 답변 '커넥션' -> 영어 키워드 'connection' 매칭 테스트"""
    user_answer = "커넥션을 맺어서 통신한다."
    keywords = ["connection"]
    result = keyword_matcher.find_similar_keywords(user_answer, keywords)
    assert "connection" in result["included_keywords"]


def test_english_to_korean_translation_matching():
    """영어 답변 'connection' -> 한글 키워드 '연결' 매칭 테스트"""
    user_answer = "connection을 통해 데이터를 전송한다."
    keywords = ["연결"]
    result = keyword_matcher.find_similar_keywords(user_answer, keywords)
    assert "연결" in result["included_keywords"]
