import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path


BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
MAIN_CATEGORY = os.environ.get("MAIN_CATEGORY", "backend")
TOKEN_FILE = Path(os.environ.get("TOKEN_FILE", "/Users/hoy/IdeaProjects/javis/k6-scripts/access-tokens-benchmark-admin-1m.csv"))
OUTPUT_FILE = Path(os.environ.get("OUTPUT_FILE", "/Users/hoy/IdeaProjects/javis/k6-scripts/question-ids-benchmark-1m.csv"))
COUNT = int(os.environ.get("COUNT", "15000"))
HTTP_TIMEOUT_SECONDS = float(os.environ.get("HTTP_TIMEOUT_SECONDS", "10"))


def read_tokens(path: Path):
    return [
        token.strip()
        for token in path.read_text().split(",")
        if token.strip()
    ]


def start_interview(token: str):
    request = urllib.request.Request(
        f"{BASE_URL}/api/interviews/start/{MAIN_CATEGORY}",
        method="POST",
        headers={
            "Cookie": f"access-token={token}",
        },
    )
    with urllib.request.urlopen(request, timeout=HTTP_TIMEOUT_SECONDS) as response:
        body = json.loads(response.read().decode("utf-8"))
        return body["questionId"]


def main():
    tokens = read_tokens(TOKEN_FILE)
    if len(tokens) < COUNT:
        raise SystemExit(f"need at least {COUNT} tokens, got {len(tokens)}")

    question_ids = []
    failures = []

    for index, token in enumerate(tokens[:COUNT], start=1):
        try:
            question_id = start_interview(token)
            question_ids.append(str(question_id))
        except urllib.error.HTTPError as exc:
            failures.append((index, exc.code))
        except Exception as exc:
            failures.append((index, str(exc)))

        if index % 500 == 0:
            print(f"prepared {index} / {COUNT}", file=sys.stderr)

    OUTPUT_FILE.write_text(",".join(question_ids))
    print(json.dumps({
        "requested": COUNT,
        "prepared": len(question_ids),
        "failures": len(failures),
        "failure_samples": failures[:20],
        "output_file": str(OUTPUT_FILE),
    }, ensure_ascii=False))

    if failures:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
