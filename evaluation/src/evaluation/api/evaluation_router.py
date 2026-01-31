import logging

from fastapi import APIRouter, BackgroundTasks

from evaluation.schemas.evaluation_schema import (
    EvaluationRequest,
    EvaluationResponse,
)
from evaluation.services.evaluation_callback_service import process_and_callback

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post("/evaluate", response_model=EvaluationResponse)
async def evaluate(
    req: EvaluationRequest,
    background_tasks: BackgroundTasks,
) -> EvaluationResponse:
    """비동기 채점 요청 수신. 백그라운드에서 채점 후 콜백 URL로 결과 전송"""
    logger.info(f"[API] 채점 요청 수신: answerId={req.answerId}")
    background_tasks.add_task(process_and_callback, req)
    return EvaluationResponse(status="accepted", answerId=req.answerId)