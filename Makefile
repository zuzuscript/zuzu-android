MAKE_ENV_FILE := $(CURDIR)/.env
export MAKE_ENV_FILE

SHELL := $(CURDIR)/scripts/make-env-shell.sh
.SHELLFLAGS := -eu -o pipefail -c

ZUZU_JS_DIR := git_modules/zuzu-js
ZUZU_JS_INSTALL_STAMP := $(ZUZU_JS_DIR)/node_modules/.make-install-stamp
ZUZU_BROWSER_BUNDLE := $(ZUZU_JS_DIR)/dist/zuzu-browser.js
APK := app/build/outputs/apk/debug/app-debug.apk

.PHONY: all apk bootstrap browser-bundle check-android help install js-deps submodules sync-browser-bundle

all: apk

help:
	@printf '%s\n' \
		'Targets:' \
		'  make bootstrap            Install Android SDK packages via sdkmanager' \
		'  make apk                  Build the Zuzu browser bundle and debug APK' \
		'  make install              Build and install the debug APK with adb' \
		'  make submodules           Initialise git submodules' \
		'  make browser-bundle       Build git_modules/zuzu-js/dist/zuzu-browser.js' \
		'  make sync-browser-bundle  Copy the browser bundle into Android assets'

check-android:
	@if [ -z "$${ANDROID_SDK_ROOT:-}" ]; then \
		echo 'Set ANDROID_SDK_ROOT before building.' >&2; \
		exit 1; \
	fi

bootstrap: check-android
	scripts/bootstrap-android-cli.sh

submodules:
	git submodule update --init --recursive

$(ZUZU_JS_INSTALL_STAMP): $(ZUZU_JS_DIR)/package.json $(ZUZU_JS_DIR)/package-lock.json | submodules
	cd $(ZUZU_JS_DIR) && npm install
	mkdir -p "$(dir $@)"
	touch "$@"

js-deps: $(ZUZU_JS_INSTALL_STAMP)

$(ZUZU_BROWSER_BUNDLE): $(ZUZU_JS_INSTALL_STAMP)
	cd $(ZUZU_JS_DIR) && bin/build-browser-bundle

browser-bundle: $(ZUZU_BROWSER_BUNDLE)

sync-browser-bundle: $(ZUZU_BROWSER_BUNDLE)
	scripts/sync-zuzu-browser-bundle.sh

apk: check-android submodules sync-browser-bundle
	scripts/build-debug-apk.sh

install: apk
	@if [ ! -x "$${ANDROID_SDK_ROOT}/platform-tools/adb" ]; then \
		echo "adb not found at: $${ANDROID_SDK_ROOT}/platform-tools/adb" >&2; \
		exit 1; \
	fi
	"$${ANDROID_SDK_ROOT}/platform-tools/adb" install -r "$(APK)"
