#!/usr/bin/env bash
# Loads the repo-root .env file into the current process's environment,
# then runs the Spring Boot backend. This is the normal way to start the
# backend locally — no manual `export` steps needed.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

if [ ! -f "$ENV_FILE" ]; then
  echo "No .env file found at $ENV_FILE. Copy .env.example to .env at the repo root and fill in real values first." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

echo "Loaded environment from $ENV_FILE"
cd "$SCRIPT_DIR"
exec mvn spring-boot:run
