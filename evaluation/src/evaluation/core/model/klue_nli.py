import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification

MODEL_ID = "this2sho/klue-nli-finetuned"

tokenizer = AutoTokenizer.from_pretrained(MODEL_ID)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_ID)
model.eval()

label_map = {0: "entailment", 1: "neutral", 2: "contradiction"}

def predict_nli(premise: str, hypothesis: str) -> str:
    # 입력 문장 토크나이징
    inputs = tokenizer(
        premise,
        hypothesis,
        return_tensors="pt",
        truncation=True,
        padding=True,
        max_length=128
    )

    # GPU가 있으면 GPU로, 없으면 CPU로
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model.to(device)
    inputs = {k: v.to(device) for k, v in inputs.items()}

    # 예측
    with torch.no_grad():
        outputs = model(**inputs)
        logits = outputs.logits
        pred = torch.argmax(logits, dim=-1).item()

    return label_map[pred]
