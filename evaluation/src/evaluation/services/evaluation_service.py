from evaluation.core.evaluator import evaluate
from evaluation.schemas.evaluation_schema import EvaluationInput, EvaluationResult


def evaluate_answer(req: EvaluationInput) -> EvaluationResult:
    reference = {
        "answers": [req.referenceAnswer],
        "keywords": req.keywords,
    }
    result = evaluate(req.userAnswer, reference)

    return EvaluationResult(
        grade=result.grade.value,
        feedback=result.feedback,
    )