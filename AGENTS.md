# ZuzuScript Android REPL

This repository contains an Android REPL prototype for ZuzuScript. The app
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

- `app/src/main/java/org/zuzuscript/repl/` contains the Kotlin app code.
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
make install
```

`ANDROID_SDK_ROOT` must be set before Android build or install targets run.
`make apk` initializes submodules, installs `zuzu-js` Node dependencies when
needed, builds the browser bundle, syncs it into Android assets, and builds
the debug APK.

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

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
- The current app is an MVP scaffold; keep changes narrow unless the task
  explicitly asks for broader product work.
