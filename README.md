# Inkrypt

Private, encrypted journal for Android. Everything stays on your device. No account, no cloud.

[**Download APK**](https://github.com/youssof20/Inkrypt/blob/main/app/release/app-release.apk) *(latest release)*

## What it does

- Journal entries with optional tags and images
- PIN lock (and optional biometric). Forgot PIN? You can reset; data is wiped.
- Templates, search, export (Markdown or encrypted ZIP), import
- Quick-capture widget, share-from-other-apps
- Dark/light theme in settings
- Offline only. No analytics.

## Build

Android 8.0+. Needs JDK 11+.

```bash
./gradlew assembleDebug
```

APK: `app/release`

Release: `./gradlew assembleRelease`. Sign the APK yourself (see Android docs on signing). Publish SHA-256 with releases so people can verify.

## License

MIT. See [LICENSE](LICENSE). 
