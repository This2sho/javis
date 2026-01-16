from dataclasses import dataclass

from evaluation.core.grade import Grade


@dataclass
class EvaluationResult:
    grade: Grade
    feedback: str

    def __init__(self, grade: Grade, missing_keywords: list[str]):
        self.grade = grade
        self.feedback = self.make_feedback(grade, missing_keywords)

    def __str__(self):
        return self.grade.value + ": " + self.feedback

    def make_feedback(self, grade: Grade, missing_keywords: list[str]) -> str:
        if grade == Grade.PERFECT:
            return "완벽한 답변입니다."
        keywords_str = ", ".join(f"‘{kw}’" for kw in missing_keywords)

        if grade == Grade.GOOD:
            return f"좋습니다. {keywords_str} 키워드들을 보충하면 완변한 답변이 될 것 같습니다."

        if grade == Grade.VAGUE:
            return f"다시 한번 생각해보세요. {keywords_str} 키워드들을 넣어서 답변을 구성하면 좋을 거 같습니다."

        return "틀렸습니다."
