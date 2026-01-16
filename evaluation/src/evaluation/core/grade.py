from enum import Enum

class Grade(str, Enum):
    PERFECT = "perfect"
    GOOD = "good"
    VAGUE = "vague"
    INCORRECT = "incorrect"
