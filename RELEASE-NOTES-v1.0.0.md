# TRA Android v1.0.0 — Fresh Start

This is a brand-new Android project created from zero for a new GitHub repository.

## Requirements carried forward

- Original TRA logo only.
- Original logo used for native intro.
- App launcher icon derived from the original logo without redesign.
- No native top toolbar.
- WordPress footer hidden inside the Android app by default.
- Native icon-based bottom navigation.
- Home, Products, Events, Gallery and Book destinations.
- Login/account cookies.
- Profile/booking file uploads.
- Android downloads.
- Trusted TRA links stay inside the app.
- External telephone/email/SMS/maps/web links open outside the WebView.
- Native retry state if the website cannot load.
- WebView renderer failure handled without killing the Activity.

## Deliberately not carried forward

- No Raspberry Pi files.
- No previous v2.x/v3.x Android source.
- No separate SplashActivity.
- No permanent MutationObserver.
- No repeated DOM scanning.
- No native blue toolbar.
- No direct intent:// execution.
- No R8/minification/resource shrinking.
- No custom sanity/lint build gates.

## Build toolchain

- Android Gradle Plugin 8.13.2
- Gradle 8.13
- Java 17
- compileSdk 36
- targetSdk 36
- minSdk 26

The normal GitHub Action only performs the required environment setup and the real Android Gradle build.
