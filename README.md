# zuzu-android

`zuzu-android` is an Android app prototype for running a Zuzu REPL.

It uses a local `WebView` that hosts the `zuzu-js` browser bundle and
bridges evaluations from Kotlin to JavaScript.

## Current status

This is an MVP scaffold with:

- a single-screen REPL UI (`Run` and `Clear`)
- a hidden runtime `WebView`
- an eval bridge (`ZuzuBridge`) from Kotlin to JS
- runtime bundle sync script from `git_modules/zuzu-js`

## Build without Android Studio (CLI workflow)

You can build everything from command line tools.
Android Studio is optional.

### 1) Install prerequisites

- Java 17+
- Gradle 8.14+
- Android command-line tools
- Android SDK platform 35 and build-tools 35.0.0
- Node.js and npm, for building the `zuzu-js` browser bundle

Set environment variables:

```bash
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"
```

Install required SDK packages:

```bash
make bootstrap
```

### 2) Sync the Zuzu browser runtime

From repository root:

```bash
make sync-browser-bundle
```

This copies:

- `git_modules/zuzu-js/dist/zuzu-browser.js`
- to `app/src/main/assets/zuzu-browser.js`

### 3) Build debug APK

```bash
make apk
```

APK output:

- `app/build/outputs/apk/debug/app-debug.apk`

### 4) Install on attached device/emulator

```bash
make install
```

## Android Studio workflow (optional)

1. Open this repository in Android Studio.
2. Let Gradle sync.
3. Run app on emulator/device.

## Smoke check

Try expression:

```text
5 mod 2
```

If the runtime bundle is not synced, the app runs in stub mode and
returns a notice in output.

## Next steps

- Persist REPL history with Room or DataStore.
- Add multiline editor affordances and command recall.
- Stream stdout and structured errors from runtime.
- Add instrumentation smoke tests and CI tasks.
