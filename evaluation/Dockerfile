FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

RUN python -m pip install --upgrade pip

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY src/ src/

# 데이터 디렉토리 생성
RUN mkdir -p /app/data

ENV HF_HOME=/app/.cache/huggingface

CMD ["python", "-m", "uvicorn", "evaluation.main:app", "--app-dir", "src", "--host", "0.0.0.0", "--port", "8000"]
