#!/usr/bin/env bash
set -eo pipefail

env_file="${MAKE_ENV_FILE:-.env}"

if [[ -f "$env_file" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$env_file"
  set +a
fi

exec /bin/bash "$@"
