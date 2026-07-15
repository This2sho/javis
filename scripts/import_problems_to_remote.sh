#!/bin/zsh
set -euo pipefail

if [[ $# -lt 3 || $# -gt 4 ]]; then
  echo "Usage: JWT_SECRET_KEY=... $0 <base_url> <member_id> <json_path> [role]" >&2
  exit 1
fi

if [[ -z "${JWT_SECRET_KEY:-}" ]]; then
  echo "JWT_SECRET_KEY env is required" >&2
  exit 1
fi

BASE_URL="${1%/}"
MEMBER_ID="$2"
JSON_PATH="$3"
ROLE="${4:-ADMIN}"

if [[ ! -f "$JSON_PATH" ]]; then
  echo "JSON file not found: $JSON_PATH" >&2
  exit 1
fi

TMP_MEMBER_IDS="$(mktemp)"
trap 'rm -f "$TMP_MEMBER_IDS"' EXIT
printf '%s' "$MEMBER_ID" > "$TMP_MEMBER_IDS"

TOKEN="$(
  JWT_SECRET_KEY="$JWT_SECRET_KEY" \
  MEMBER_IDS_FILE="$TMP_MEMBER_IDS" \
  TOKEN_ROLE="$ROLE" \
  python3 /Users/hoy/IdeaProjects/javis/scripts/generate_tokens_from_member_ids.py
)"

curl --fail-with-body \
  -X POST "$BASE_URL/admin/api/problems" \
  -H 'Content-Type: application/json' \
  -H "Cookie: access-token=$TOKEN" \
  --data-binary "@$JSON_PATH"
