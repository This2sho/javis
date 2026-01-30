"""
NLI 분류 모델

사용자 답변과 참조 답변 간의 관계를 분류:
- entailment: 함의 (사용자 답변이 참조 답변을 포함)
- neutral: 중립
- contradiction: 모순
"""
import logging

import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification

from evaluation.core.label import Label
from evaluation.core.sentence import Sentence

logger = logging.getLogger(__name__)

MODEL_ID = "this2sho/klue-nli-finetuned"
LABEL_MAP = {0: "entailment", 1: "neutral", 2: "contradiction"}


class NLIClassifier:
    """NLI 분류기 (싱글톤)"""

    def __init__(self):
        logger.info(f"[NLI] 모델 로드 시작: {MODEL_ID}")
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.tokenizer = AutoTokenizer.from_pretrained(MODEL_ID)
        self.model = AutoModelForSequenceClassification.from_pretrained(
            MODEL_ID,
            low_cpu_mem_usage=False
        )
        self.model.to(self.device)
        self.model.eval()
        logger.info(f"[NLI] 모델 로드 완료 (device: {self.device})")

    def predict(self, premise: str, hypothesis: str) -> str:
        """NLI 예측"""
        inputs = self.tokenizer(
            premise,
            hypothesis,
            return_tensors="pt",
            truncation=True,
            padding=True,
            max_length=128
        )
        inputs = {k: v.to(self.device) for k, v in inputs.items()}

        with torch.no_grad():
            outputs = self.model(**inputs)
            prediction = torch.argmax(outputs.logits, dim=-1).item()

        return LABEL_MAP[prediction]


_classifier: NLIClassifier | None = None


def load_classifier():
    global _classifier
    if _classifier is None:
        _classifier = NLIClassifier()


def classify(user_answer: Sentence, reference: Sentence) -> Label:
    """
    사용자 답변과 참조 답변의 NLI 관계 분류

    Args:
        user_answer: 사용자 답변
        reference: 참조 답변 (premise)

    Returns:
        Label: 분류 결과 (entailment/neutral/contradiction)
    """
    load_classifier()
    result = _classifier.predict(reference.value, user_answer.value)
    return Label(result)
