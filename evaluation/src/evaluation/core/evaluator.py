from torch.nn.functional import cosine_similarity

from evaluation.core import classification_model
from evaluation.core import embedding_model
from evaluation.core import keyword_matcher
from evaluation.core.evaluation_result import EvaluationResult
from evaluation.core.grade import Grade
from evaluation.core.keywords import Keywords
from evaluation.core.sentence import Sentence

INCORRECT_RESULT = EvaluationResult(Grade.INCORRECT, set())

def evaluate(user_answer: str, reference: dict) -> EvaluationResult:
    user_answer = Sentence(user_answer)
    reference_answers = [Sentence(answer) for answer in reference["answers"]]
    keywords = Keywords(reference["keywords"])

    classified_ok, valid_reference_answers = classify_valid_answers(user_answer, reference_answers)
    if not classified_ok:
        return INCORRECT_RESULT

    score, missing_keywords = calculate_score(user_answer, valid_reference_answers, keywords)

    if score >= 0.8:
        return EvaluationResult(Grade.PERFECT, missing_keywords)
    if score >= 0.65:
        return EvaluationResult(Grade.GOOD, missing_keywords)
    if score >= 0.5:
        return EvaluationResult(Grade.VAGUE, missing_keywords)
    return INCORRECT_RESULT

def classify_valid_answers(user_answer: Sentence, reference_answers: list[Sentence]) -> (bool, list):
    valid_reference_answers = []
    for reference_answer in reference_answers:
        result = classification_model.classify(user_answer, reference_answer)
        if result.is_contradiction():
            return False, []
        if result.is_entailment():
            valid_reference_answers.append(reference_answer)

    if len(valid_reference_answers) == 0:
        return False, []
    return True, valid_reference_answers

def has_contradiction(user_answer: Sentence, reference_answers: list[Sentence]) -> bool:
    for reference_answer in reference_answers:
        result = classification_model.classify(user_answer, reference_answer)
        if result.is_contradiction():
            return True
    return False

def calculate_score(user_answer: Sentence, reference_answers: list[Sentence], keywords: Keywords) -> (float, set):
    max_similarity_score = max(
        calculate_sentence_similarity_score(user_answer, reference_answer)
        for reference_answer in reference_answers
    )
    keyword_score_result = calculate_keyword_score(user_answer, keywords)
    similarity_weight = 0.5
    keyword_weight = 0.5
    score = max_similarity_score * similarity_weight + keyword_score_result["score"] * keyword_weight
    return score, keyword_score_result["missing_keywords"]

def calculate_sentence_similarity_score(user_answer: Sentence, reference_answer: Keywords) -> float:
    encoded_user_answer = embedding_model.encode(user_answer.value)
    encoded_reference_answer = embedding_model.encode(reference_answer.value)
    return cosine_similarity(encoded_user_answer, encoded_reference_answer, dim=0).item()

def calculate_keyword_score(user_answer: Sentence, keywords: Keywords) -> dict:
    result = keyword_matcher.find_similar_keywords(user_answer.value, keywords.value)
    return {
        "score": len(result["included_keywords"]) / len(result["keywords"]),
        "missing_keywords": result["missing_keywords"]
    }


