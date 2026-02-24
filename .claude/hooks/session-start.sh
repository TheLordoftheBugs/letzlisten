#!/bin/bash
set -euo pipefail

# Only run in remote (Claude Code on the web) environments
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

# Ensure jq is available for JSON validation/linting
if ! command -v jq &>/dev/null; then
  apt-get install -y -q jq
fi

echo "Session start hook completed successfully."
