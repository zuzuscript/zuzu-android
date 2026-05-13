#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

if [ "${ANDROID_SDK_ROOT:-}" = "" ]; then
	echo "Set ANDROID_SDK_ROOT before building." >&2
	exit 1
fi

if [ "${GRADLE:-}" != "" ]; then
	GRADLE_CMD=( "$GRADLE" )
elif [ -x "$ROOT_DIR/gradlew" ]; then
	GRADLE_CMD=( "$ROOT_DIR/gradlew" )
elif [ "${GRADLE_HOME:-}" != "" ] && [ -x "$GRADLE_HOME/bin/gradle" ]; then
	GRADLE_CMD=( "$GRADLE_HOME/bin/gradle" )
elif command -v gradle >/dev/null 2>&1; then
	GRADLE_CMD=( gradle )
else
	echo "Gradle is not installed or not in PATH." >&2
	echo "Install Gradle 8.7+, add a Gradle wrapper, or set GRADLE=/path/to/gradle in .env." >&2
	exit 1
fi

"${GRADLE_CMD[@]}" -p "$ROOT_DIR" :app:bundleRelease

echo
echo "AAB built at:"
echo "  $ROOT_DIR/app/build/outputs/bundle/release/app-release.aab"
