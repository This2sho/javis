import torch
from sentence_transformers import SentenceTransformer

# 모델 로드
model = SentenceTransformer("BM-K/KoSimCSE-roberta-multitask")

def encode(sentence):
    return torch.tensor(model.encode(sentence), dtype=torch.float32)