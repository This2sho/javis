FROM python:3.11-slim

WORKDIR /app

RUN python -m pip install --upgrade pip

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY src/ src/

ENV HF_HOME=/app/.cache/huggingface

CMD ["python", "-m", "uvicorn", "evaluation.main:app", "--app-dir", "src", "--host", "0.0.0.0", "--port", "8000"]
