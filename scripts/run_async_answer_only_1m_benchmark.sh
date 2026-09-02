#!/bin/zsh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

BASE_URL="${BASE_URL:-http://127.0.0.1:8081}"
WS_URL="${WS_URL:-ws://127.0.0.1:8081/ws/interview}"
REBUILD_IMAGE="${REBUILD_IMAGE:-1}"
HOST_PORT="${HOST_PORT:-8081}"
DOCKER_CPUS="${DOCKER_CPUS:-}"
DOCKER_CONTAINER="${DOCKER_CONTAINER:-learn-hub-backend-benchmark}"
DOCKER_IMAGE="${DOCKER_IMAGE:-javis-learn-hub-backend-test:latest}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-learn-hub-mysql-test}"
MEMBER_IDS_FILE="${MEMBER_IDS_FILE:-$ROOT_DIR/k6-scripts/member-ids-benchmark-rerun.csv}"
TRIMMED_MEMBER_IDS_FILE="${TRIMMED_MEMBER_IDS_FILE:-/tmp/member-ids-benchmark-current-16000.csv}"
TOKEN_FILE="${TOKEN_FILE:-/tmp/access-tokens-benchmark-admin-current-16000.csv}"
QUESTION_IDS_FILE="${QUESTION_IDS_FILE:-/tmp/question-ids-aligned-current-16000.csv}"
QUESTION_COUNT="${QUESTION_COUNT:-16000}"
RESULT_BASENAME="${RESULT_BASENAME:-async-answer-only-1m-current-code}"
RESULT_DIR="${RESULT_DIR:-/tmp}"
K6_SCRIPT="${K6_SCRIPT:-$ROOT_DIR/k6-scripts/async-answer-only-1m-benchmark.js}"
VUS="${VUS:-300}"
DURATION="${DURATION:-1m}"
WS_TIMEOUT_MS="${WS_TIMEOUT_MS:-15000}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx2g -Xlog:gc*,gc+heap=info:file=/logs/gc.log:time,uptime,level,tags}"
DB_URL="${DB_URL:-jdbc:mysql://learn-hub-mysql-test:3306/learn_hub_test?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Seoul}"
EVAL_CORE_POOL_SIZE="${EVAL_CORE_POOL_SIZE:-200}"
EVAL_MAX_POOL_SIZE="${EVAL_MAX_POOL_SIZE:-300}"
EVAL_QUEUE_CAPACITY="${EVAL_QUEUE_CAPACITY:-10}"

K6_JSON="$RESULT_DIR/$RESULT_BASENAME.json"
DOCKER_STATS_CSV="$RESULT_DIR/$RESULT_BASENAME-docker-stats.csv"
SUMMARY_JSON="$RESULT_DIR/$RESULT_BASENAME-summary.json"

cleanup() {
  if [[ -n "${STATS_PID:-}" ]] && kill -0 "$STATS_PID" >/dev/null 2>&1; then
    kill "$STATS_PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

echo "[1/6] prepare token input"
python3 - <<PY
from pathlib import Path
count = int("${QUESTION_COUNT}")
src = Path("${MEMBER_IDS_FILE}").read_text().strip().split(",")
member_ids = [value.strip() for value in src if value.strip()][:count]
Path("${TRIMMED_MEMBER_IDS_FILE}").write_text(",".join(member_ids))
print(len(member_ids))
PY

JWT_SECRET_KEY="$(grep '^JWT_SECRET_KEY=' "$ROOT_DIR/.env" | cut -d= -f2-)"
JWT_SECRET_KEY="$JWT_SECRET_KEY" \
MEMBER_IDS_FILE="$TRIMMED_MEMBER_IDS_FILE" \
python3 "$ROOT_DIR/scripts/generate_tokens_from_member_ids.py" > "$TOKEN_FILE"

echo "[2/6] rebuild backend image if requested"
if [[ "$REBUILD_IMAGE" == "1" ]]; then
  docker compose --profile test build learn-hub-backend-test
else
  echo "skip image rebuild"
fi

echo "[3/6] restart benchmark backend container"
docker rm -f "$DOCKER_CONTAINER" >/dev/null 2>&1 || true
docker_run_args=(
  -d
  --name "$DOCKER_CONTAINER"
  --network javis-net
  -p "${HOST_PORT}:8080"
  --env-file "$ROOT_DIR/.env"
  -e SPRING_PROFILES_ACTIVE=test
  -e DB_URL="$DB_URL"
  -e JAVA_OPTS="$JAVA_OPTS"
  -e BENCHMARK_EXECUTORS_EVALUATION_CORE_POOL_SIZE="$EVAL_CORE_POOL_SIZE"
  -e BENCHMARK_EXECUTORS_EVALUATION_MAX_POOL_SIZE="$EVAL_MAX_POOL_SIZE"
  -e BENCHMARK_EXECUTORS_EVALUATION_QUEUE_CAPACITY="$EVAL_QUEUE_CAPACITY"
  -v "$ROOT_DIR/logs:/logs"
)
if [[ -n "$DOCKER_CPUS" ]]; then
  docker_run_args+=(--cpus "$DOCKER_CPUS")
fi
docker run \
  "${docker_run_args[@]}" \
  "$DOCKER_IMAGE" >/dev/null

echo "[4/6] wait for backend health"
for _ in {1..60}; do
  if curl -fsS "$BASE_URL/actuator/health" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done
curl -fsS "$BASE_URL/actuator/health" >/dev/null

echo "[5/6] prepare aligned question pool"
BASE_URL="$BASE_URL" \
TOKEN_FILE="$TOKEN_FILE" \
OUTPUT_FILE="$QUESTION_IDS_FILE" \
COUNT="$QUESTION_COUNT" \
python3 "$ROOT_DIR/scripts/prepare_question_pool.py"

echo "[6/6] run benchmark and summarize"
rm -f "$DOCKER_STATS_CSV"
(
  for _ in {1..90}; do
    printf '%s,' "$(date +%s)" >> "$DOCKER_STATS_CSV"
    docker stats --no-stream --format '{{.Name}},{{.CPUPerc}},{{.MemUsage}}' "$DOCKER_CONTAINER" >> "$DOCKER_STATS_CSV"
    sleep 1
  done
) &
STATS_PID=$!

K6_WEB_DASHBOARD=false \
BASE_URL="$BASE_URL" \
WS_URL="$WS_URL" \
TOKEN_FILE="$TOKEN_FILE" \
QUESTION_IDS_FILE="$QUESTION_IDS_FILE" \
VUS="$VUS" \
DURATION="$DURATION" \
WS_TIMEOUT_MS="$WS_TIMEOUT_MS" \
k6 run --summary-export="$K6_JSON" "$K6_SCRIPT"

wait "$STATS_PID" || true
python3 "$ROOT_DIR/scripts/summarize_async_answer_benchmark.py" "$K6_JSON" "$DOCKER_STATS_CSV" | tee "$SUMMARY_JSON"

echo
echo "eval_core_pool_size=$EVAL_CORE_POOL_SIZE"
echo "eval_max_pool_size=$EVAL_MAX_POOL_SIZE"
echo "eval_queue_capacity=$EVAL_QUEUE_CAPACITY"
echo "docker_cpus=${DOCKER_CPUS:-unlimited}"
echo "k6_json=$K6_JSON"
echo "docker_stats=$DOCKER_STATS_CSV"
echo "summary_json=$SUMMARY_JSON"
