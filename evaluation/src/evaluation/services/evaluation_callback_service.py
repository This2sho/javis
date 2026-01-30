import logging
import time

import httpx

from evaluation.schemas.evaluation_schema import (
    EvaluationInput,
    EvaluationRequest,
    EvaluationCallback,
)
from evaluation.services.evaluation_service import evaluate_answer

logger = logging.getLogger(__name__)


async def process_and_callback(req: EvaluationRequest) -> None:
    """백그라운드에서 채점 수행 후 콜백 전송"""
    total_start = time.perf_counter()
    try:
        logger.info(f"[CALLBACK] 백그라운드 채점 시작: answerId={req.answerId}")

        # 1. 채점 수행
        eval_start = time.perf_counter()
        result = _run_evaluation(req)
        eval_time = (time.perf_counter() - eval_start) * 1000
        logger.info(f"[CALLBACK] 채점 완료: answerId={req.answerId}, {eval_time:.2f}ms, grade={result.grade}")

        # 2. 콜백 전송
        callback_start = time.perf_counter()
        await _send_callback(req, result)
        callback_time = (time.perf_counter() - callback_start) * 1000
        logger.info(f"[CALLBACK] 콜백 전송 완료: answerId={req.answerId}, {callback_time:.2f}ms")

        total_time = (time.perf_counter() - total_start) * 1000
        logger.info(f"[CALLBACK] 전체 완료: answerId={req.answerId}, 총 {total_time:.2f}ms")

    except Exception as e:
        total_time = (time.perf_counter() - total_start) * 1000
        logger.error(f"[CALLBACK] 실패: answerId={req.answerId}, {total_time:.2f}ms, error={e}")


def _run_evaluation(req: EvaluationRequest):
    """채점 수행 (동기)"""
    eval_input = EvaluationInput(
        referenceAnswer=req.referenceAnswer,
        keywords=req.keywords,
        userAnswer=req.userAnswer,
    )
    return evaluate_answer(eval_input)


async def _send_callback(req: EvaluationRequest, result) -> None:
    """채점 결과를 콜백 URL로 전송"""
    callback = EvaluationCallback(
        answerId=req.answerId,
        grade=result.grade,
        feedback=result.feedback,
    )

    async with httpx.AsyncClient(timeout=30.0) as client:
        response = await client.post(
            req.callbackUrl,
            json=callback.model_dump(),
        )
        response.raise_for_status()