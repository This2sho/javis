from evaluation.core.evaluator import evaluate, calculate_keyword_score
from evaluation.core.grade import Grade
from evaluation.core.keywords import Keywords
from evaluation.core.sentence import Sentence


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
    reference = {"answers": ["tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이다.."], "keywords": ["TCP", "연결", "신뢰성", "보장"]}
    result = evaluate(user_answer, reference)
    assert result.grade == Grade.INCORRECT

# 완전히 상반되는 내용이지만 문장이 유사하고 핵심 키워드를 포함한 답변에 점수가 낮게 가도록 해야함
def test_evaluate_incorrect2():
    user_answer = "tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이 아니다."
    reference = {"answers": ["tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이다.."], "keywords": ["TCP", "연결", "신뢰성", "보장"]}
    result = evaluate(user_answer, reference)
    assert result.grade == Grade.INCORRECT

def test_evaluate_incorrect3():
    user_answer = "사용자가 tcp는 비연결 중심의 신뢰성을 보장하지않는 프로토콜이다."
    reference = {"answers": ["tcp는 연결 중심의 신뢰성을 보장하는 프로토콜이다.."], "keywords": ["TCP", "연결", "신뢰성", "보장"]}
    result = evaluate(user_answer, reference)
    assert result.grade == Grade.INCORRECT

# 이후 해당 테스트도 통과하도록 개선
def test_low_keyword_score():
    user_answer = "커넥션 타임아웃 설정은 잘해야한다."
    keywords = ["TCP", "연결", "신뢰성", "보장"]
    result = calculate_keyword_score(Sentence(user_answer), Keywords(keywords))
    score = result["score"]
    expected_score = 1/4 # 커넥션 = 연결
    assert score == expected_score

def test_low_keyword_score2():
    user_answer = "connection이 있다"
    keywords = ["TCP", "커넥션", "신뢰성", "보장"]
    result = calculate_keyword_score(Sentence(user_answer), Keywords(keywords))
    score = result["score"]
    expected_score = 1/4 # TCP, 연결, 신뢰성
    assert score == expected_score

def test_mid_keyword_score():
    user_answer = "TCP는 연결이다."
    keywords = ["TCP", "연결", "신뢰성", "보장"]
    result = calculate_keyword_score(Sentence(user_answer), Keywords(keywords))
    score = result["score"]
    expected_score = 2/4 # TCP, 연결
    assert score == expected_score

def test_high_keyword_score():
    user_answer = "TCP는 연결 지향으로 신뢰성을 가진다."
    keywords = ["TCP", "연결", "신뢰성", "보장"]
    result = calculate_keyword_score(Sentence(user_answer), Keywords(keywords))
    score = result["score"]
    expected_score = 3/4 # TCP, 연결, 신뢰성
    assert score == expected_score
