from fastapi import FastAPI

from evaluation.api.evaluation_router import router as evaluation_router

app = FastAPI(title="Evaluation Service")

app.include_router(
    evaluation_router,
    tags=["evaluation"]
)
