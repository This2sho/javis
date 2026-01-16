from pydantic import BaseModel

class EvaluationRequest(BaseModel):
    problem: str
    referenceAnswer: str
    keywords: list[str]
    userAnswer: str

class EvaluationResponse(BaseModel):
    grade: str
    feedback: str
