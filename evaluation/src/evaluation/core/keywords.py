from dataclasses import dataclass

@dataclass
class Keywords:
    value: list[str]

    def __init__(self, value: list[str]):
        self.value = [k.lower() for k in value]

    def __str__(self):
        return self.value