#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../../.." && pwd )"
SOURCE="$ROOT_DIR/extras/zuzu-js/dist/zuzu-browser.js"
TARGET="$ROOT_DIR/extras/zuzu-android/app/src/main/assets/zuzu-browser.js"

if [ ! -f "$SOURCE" ]; then
	echo "Source bundle not found: $SOURCE" >&2
	echo "Build it via extras/zuzu-js/bin/build-browser-bundle" >&2
	exit 1
fi

cp "$SOURCE" "$TARGET"
echo "Copied $SOURCE -> $TARGET"
