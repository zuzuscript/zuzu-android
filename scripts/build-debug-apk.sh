#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

if [ "${ANDROID_SDK_ROOT:-}" = "" ]; then
	echo "Set ANDROID_SDK_ROOT before building." >&2
	exit 1
fi

if command -v gradle >/dev/null 2>&1; then
	GRADLE_CMD=( gradle )
else
	echo "Gradle is not installed or not in PATH." >&2
	echo "Install Gradle 8.14+ or add a Gradle wrapper." >&2
	exit 1
fi

"${GRADLE_CMD[@]}" -p "$ROOT_DIR" :app:assembleDebug

echo
echo "APK built at:"
echo "  $ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
