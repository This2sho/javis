from dataclasses import dataclass

@dataclass
class Sentence:
    value: str

    def __init__(self, value: str):
        self.value = value.lower()

    def __str__(self):
        return self.value