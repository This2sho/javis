from pydantic import BaseModel


# 내부 서비스용
class EvaluationInput(BaseModel):
    referenceAnswer: str
    keywords: list[str]
    userAnswer: str


class EvaluationResult(BaseModel):
    grade: str
    feedback: str


# API용
class EvaluationRequest(BaseModel):
    answerId: int
    referenceAnswer: str
    keywords: list[str]
    userAnswer: str
    callbackUrl: str


class EvaluationResponse(BaseModel):
    status: str
    answerId: int


# 콜백용
class EvaluationCallback(BaseModel):
    answerId: int
    grade: str
    feedback: str