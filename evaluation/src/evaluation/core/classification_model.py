from evaluation.core.label import Label
from evaluation.core.model import klue_nli
from evaluation.core.sentence import Sentence


def classify(user_answer: Sentence, reference: Sentence) -> Label:
    result = klue_nli.predict_nli(reference.value, user_answer.value)
    return Label(result)