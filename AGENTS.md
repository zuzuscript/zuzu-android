# ZuzuScript Android REPL

This repository contains a beta-quality Android REPL for ZuzuScript. The app
uses the JavaScript implementation by loading the `zuzu-js` browser bundle
inside a local hidden WebView.

Use Oxford English in documentation: mostly standard British English, with
`-ize` word endings.

## Relationship To Other Projects

`zuzu-android` is a host application for `zuzu-js`, not a separate language
implementation. It consumes `git_modules/zuzu-js` as a submodule and copies
the generated browser runtime into Android assets.

Do not edit `git_modules/zuzu-js` unless the task explicitly targets the
JavaScript runtime. If you do work there, read `git_modules/zuzu-js/AGENTS.md`
and keep the submodule change intentional.

## Project Shape

- `app/src/main/java/org/zuzulang/repl/` contains the Kotlin app code.
- `app/src/main/assets/index.html` hosts the runtime WebView.
- `app/src/main/assets/zuzu-runtime-bridge.js` bridges Kotlin and JS.
- `app/src/main/assets/zuzu-browser.js` is the synced browser bundle.
- `scripts/` contains CLI helpers used by the Makefile.
- `git_modules/zuzu-js/` is the JavaScript runtime submodule.

Kotlin sends REPL input into the WebView through `ZuzuBridge`, and
JavaScript returns rendered output to the Android UI.

## Build Workflow

Use the Makefile targets rather than duplicating build logic:

```bash
make bootstrap
make sync-browser-bundle
make apk
make aab
make install
```

`ANDROID_SDK_ROOT` must be set before Android build or install targets run.
`make apk` and `make aab` initialize submodules, install `zuzu-js` Node
dependencies when needed, build the browser bundle, sync it into Android
assets, and build the release package.

The release APK is written to:

```text
app/build/outputs/apk/release/app-release.apk
```

The release Android App Bundle is written to:

```text
app/build/outputs/bundle/release/app-release.aab
```

The release variant is signed with Android's local debug signing config for
beta sideloading unless release signing environment variables are set:

```text
ZUZU_ANDROID_KEYSTORE
ZUZU_ANDROID_KEYSTORE_PASSWORD
ZUZU_ANDROID_KEY_ALIAS
ZUZU_ANDROID_KEY_PASSWORD
```

Do not commit keystores or signing secrets. `ZUZU_ANDROID_KEY_PASSWORD` may
be omitted when it is the same as the keystore password.

## Runtime Bundle Rules

The source bundle is:

```text
git_modules/zuzu-js/dist/zuzu-browser.js
```

The Android asset copy is:

```text
app/src/main/assets/zuzu-browser.js
```

Keep changes to the synced runtime bundle intentional. If only Android app
code changed, do not regenerate or modify the bundle.

## Style And Maintenance

- Prefer Kotlin and Gradle conventions already present in the app.
- Keep Bash scripts strict with `set -euo pipefail`.
- Prefer existing scripts and Makefile targets over adding parallel build
  paths.
- Keep documentation and paths in this split-repository layout, not older
  monorepo `extras/` paths.
- The current app is beta-quality software; keep changes narrow and preserve
  the REPL workflow unless the task explicitly asks for broader product work.
