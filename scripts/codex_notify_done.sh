#!/bin/zsh
set -euo pipefail

TITLE="${1:-Codex}"
MESSAGE="${2:-작업이 완료되었습니다.}"
SUBTITLE="${3:-javis}"

if command -v terminal-notifier >/dev/null 2>&1; then
  terminal-notifier \
    -title "$TITLE" \
    -subtitle "$SUBTITLE" \
    -message "$MESSAGE"
  exit 0
fi

/usr/bin/osascript -e "display notification \"${MESSAGE//\"/\\\"}\" with title \"${TITLE//\"/\\\"}\" subtitle \"${SUBTITLE//\"/\\\"}\""
