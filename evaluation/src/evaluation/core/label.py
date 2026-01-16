from enum import Enum

class Label(str, Enum):
    ENTAILMENT = "entailment"
    NEUTRAL = "neutral"
    CONTRADICTION = "contradiction"

    def is_contradiction(self) -> bool:
        return self == Label.CONTRADICTION

    def is_entailment(self) -> bool:
        return self == Label.ENTAILMENT