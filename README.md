# zuzu-android

`zuzu-android` is a beta-quality Android app for running a ZuzuScript REPL.

It uses a local `WebView` that hosts the `zuzu-js` browser bundle and
bridges evaluations from Kotlin to JavaScript.

## Current status

This is beta software with:

- a single-screen REPL UI with a multiline monospace editor and scrollable
  output
- syntax highlighting and simple block indentation in the editor
- a hidden runtime `WebView`
- a script execution bridge (`ZuzuBridge`) from Kotlin to JS
- runtime bundle sync script from `git_modules/zuzu-js`
- Android app icon assets for launcher and adaptive icons

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

### 3) Build release package

```bash
make apk
```

APK output:

- `app/build/outputs/apk/release/app-release.apk`

For Google Play Console uploads, build the Android App Bundle:

```bash
make aab
```

AAB output:

- `app/build/outputs/bundle/release/app-release.aab`

By default the CLI release build uses Android's local debug signing key so
that the beta APK can be installed directly on a test device.

To sign with a private release keystore, set these environment variables
before running `make apk` or `make aab`:

```bash
export ZUZU_ANDROID_KEYSTORE="/path/to/release.jks"
export ZUZU_ANDROID_KEYSTORE_PASSWORD="..."
export ZUZU_ANDROID_KEY_ALIAS="..."
export ZUZU_ANDROID_KEY_PASSWORD="..."
```

If the key password is the same as the keystore password, you can omit
`ZUZU_ANDROID_KEY_PASSWORD`.

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

If the runtime bundle is not synced or cannot load, the app reports runtime
diagnostics in the output area.

## Next steps

- Persist REPL history with Room or DataStore.
- Add command recall.
- Stream stdout and structured errors from runtime.
- Add instrumentation smoke tests and CI tasks.
