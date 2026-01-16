from fastapi import APIRouter

from evaluation.schemas.evaluation_schema import EvaluationResponse, EvaluationRequest
from evaluation.services.evaluation_service import evaluate_answer

router = APIRouter()

@router.post("/evaluate", response_model=EvaluationResponse)
def evaluate(req: EvaluationRequest) -> EvaluationResponse:
    return evaluate_answer(req)
