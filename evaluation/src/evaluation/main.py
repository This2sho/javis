import logging
import sys
from contextlib import asynccontextmanager

from fastapi import FastAPI

from evaluation.api.evaluation_router import router as evaluation_router
from evaluation.core import classification_model, keyword_matcher


# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)

# 외부 라이브러리 로그 레벨 조정
logging.getLogger("httpx").setLevel(logging.WARNING)
logging.getLogger("httpcore").setLevel(logging.WARNING)

logger = logging.getLogger(__name__)


# ===== lifespan 정의 =====
@asynccontextmanager
async def lifespan(app: FastAPI):
    # === startup ===
    logger.info("Evaluation Service 시작 (lifespan)")
    classification_model.load_classifier()
    keyword_matcher.load_fasttext()

    yield
    # === shutdown ===
    logger.info("Evaluation Service 종료")


# ===== FastAPI 앱 생성 =====
app = FastAPI(
    title="Evaluation Service",
    lifespan=lifespan
)

app.include_router(
    evaluation_router,
    tags=["evaluation"]
)


@app.get("/health")
def health_check():
    return {"status": "healthy"}