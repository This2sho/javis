from enum import Enum


class Label(str, Enum):
    ENTAILMENT = "entailment"
    NEUTRAL = "neutral"
    CONTRADICTION = "contradiction"

    def is_contradiction(self) -> bool:
        return self == Label.CONTRADICTION

    def is_entailment(self) -> bool:
        return self == Label.ENTAILMENT

    def is_neutral(self) -> bool:
        return self == Label.NEUTRAL

    def get_score(self) -> float:
        """NLI 라벨에 따른 점수 반환"""
        if self == Label.ENTAILMENT:
            return 0.5
        if self == Label.NEUTRAL:
            return 0.25
        return 0.0  # CONTRADICTION
