# 채점 모델 설명

본 문서는 Evaluation Server에서 사용하는
자연어 처리 모델의 구성과 학습 방식을 설명합니다.

---

## 사용 모델

### 1. NLI 모델

- Base Model: klue/bert-base
- Fine-tuned Model: this2sho/klue-nli-finetuned
- Task: Natural Language Inference
- Labels:
  - entailment (함의)
  - neutral (중립)
  - contradiction (모순)

NLI 모델은 사용자 답변과 모범 답변 간의
논리적 관계를 판단하는 데 사용됩니다.

#### 점수 계산
| 라벨 | 점수 | 설명 |
|------|------|------|
| entailment | 0.5 | 답변이 모범 답변을 함의 |
| neutral | 0.25 | 부분적으로 관련 있음 |
| contradiction | 0 | 모순 → 즉시 INCORRECT |

---

### 2. 키워드 매칭 모델 (FastText)

- Model: FastText `cc.en.50.bin` (영어 50차원)
- 크기: ~1.24GB
- 용도: 단어 간 의미적 유사도 계산

#### 기존 모델 (KoSimCSE-roberta)
이전에는 `BM-K/KoSimCSE-roberta-multitask` 모델을 사용했으나,
다음 문제로 인해 FastText로 교체되었습니다:

- **속도 문제**: 사용자 답변에 포함된 여러 핵심 단어와 핵심 키워드를 임베딩해야하는데, 여러 단어를 CPU 환경에서 임베딩하기에 느림
- **타임아웃**: 동시 요청 시 응답 지연으로 타임아웃 발생

#### 현재 구조 (FastText + 번역)
```
한글 키워드/단어
      │
      v
┌─────────────────┐
│ 형태소 분석      │  kiwipiepy
│ (단어 추출)      │  "멀티스레드" → ["멀티", "스레드"]
└─────────────────┘
      │
      v
┌─────────────────┐
│ 한→영 번역      │  argostranslate
│                 │  "멀티" → "multi"
└─────────────────┘
      │
      v
┌─────────────────┐
│ FastText        │  cosine_similarity(word1, word2)
│ 유사도 계산      │  threshold: 0.65
└─────────────────┘
```

#### 장점
- **속도**: 기존 벡터 연산 대신 학습된 fasttext 모델으로 미리 계산된 결과를 메모리에서 읽어와 빠른 유사도 계산
- **안정성**: 타임아웃 없이 안정적 처리

---

### 3. 번역 모델 (deep_translator)

- 라이브러리: deep_translator 사용
- 언어 쌍: 한국어(ko) → 영어(en)
- 용도: 한글 단어를 영어로 번역하여 FastText 입력으로 사용

---

## NLI 모델 학습 데이터

### KLUE-NLI (공식 데이터셋)

- 출처: Hugging Face Datasets Hub (`klue / nli`)
- 용도: 한국어 문장 간 기본적인 논리 추론 학습

---

### CS 도메인 추가 학습 데이터

CS 인터뷰 답변 특성을 반영하기 위해
알고리즘, 자료구조, CS 개념 설명 문장을 중심으로
약 1,500개의 문장 쌍을 직접 구성하여 추가 학습에 사용했습니다.

예시:

- Premise
  배열의 탐색과 접근은 O(1)이지만, 중간 삽입과 삭제는 원소 이동으로 인해 O(n)입니다.

- Hypothesis
  탐색과 접근은 O(1)이고, 삽입과 삭제는 O(n)입니다.

- Label
  entailment

---

## 파인튜닝 전략

NLI 모델은 다음 단계로 학습되었습니다.

1. KLUE-NLI 데이터로 일반 NLI 학습
2. CS 도메인 데이터로 추가 미세 조정 (continued fine-tuning)

CS 데이터 학습 시에는
저학습률(2e-6)을 사용하여
기존 언어 추론 성능을 유지하면서 도메인 적응에 집중했습니다.

---

## 설계 판단

### 기존 접근 (문장 유사도 + 키워드)
- 문장 유사도만으로는 논리적 오류를 탐지하기 어려움
- NLI 모델의 역할과 중복되는 부분 존재

### 현재 접근 (NLI + FastText 키워드)
- **NLI 분류**: 논리적 관계 판단 (함의/중립/모순)
- **FastText 키워드**: 핵심 개념 포함 여부 확인

두 가지를 분리하여 각각의 역할에 집중:
- NLI → "답변이 논리적으로 맞는가?"
- 키워드 → "핵심 개념을 언급했는가?"

---

## 모델 파일 위치

```
evaluation/src/evaluation/core/model/
├── cc.en.50.bin     # FastText 영어 50차원 모델
└── word_extractor.py # 형태소 분석 (kiwipiepy)
```

NLI 모델은 Hugging Face Hub에서 자동 다운로드됩니다:
- `this2sho/klue-nli-finetuned`
