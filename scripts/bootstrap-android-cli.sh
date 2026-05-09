#!/usr/bin/env bash
set -euo pipefail

if [ "${ANDROID_SDK_ROOT:-}" = "" ]; then
	echo "Set ANDROID_SDK_ROOT before running this script." >&2
	exit 1
fi

SDKMANAGER="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$SDKMANAGER" ]; then
	echo "sdkmanager not found at: $SDKMANAGER" >&2
	echo "Install Android command-line tools first." >&2
	exit 1
fi

"$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" \
	"platform-tools" \
	"platforms;android-35" \
	"build-tools;35.0.0"

echo "Android CLI prerequisites installed."
