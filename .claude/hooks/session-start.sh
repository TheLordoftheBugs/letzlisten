#!/bin/bash
set -euo pipefail

# Only run in remote (Claude Code on the web) environments
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

echo "Session start hook running..."

# Ensure git is configured for safe directory access
git config --global --add safe.directory "${CLAUDE_PROJECT_DIR:-$(pwd)}"

echo "Session start hook complete."
