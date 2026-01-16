from evaluation.core.evaluator import evaluate
from evaluation.schemas.evaluation_schema import EvaluationResponse, EvaluationRequest


def evaluate_answer(req: EvaluationRequest) -> EvaluationResponse:
    reference = {
        "answers": [req.referenceAnswer],
        "keywords": req.keywords,
    }
    result = evaluate(req.userAnswer, reference)

    return EvaluationResponse(
        grade=result.grade.value,
        feedback=result.feedback,
    )
