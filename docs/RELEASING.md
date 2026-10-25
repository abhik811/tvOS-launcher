# Release procedure

The preview is development-signed and uses `com.tvos.launcher.preview`. Production uses `com.tvos.launcher`. Public release keys are not stored in this repository. Keep a secure private backup of the release key; updates require the same certificate.

Create a private key outside the repository with keytool or Android Studio, then supply all four variables privately:

- TVOS_KEYSTORE — absolute keystore path
- TVOS_STORE_PASSWORD
- TVOS_KEY_ALIAS
- TVOS_KEY_PASSWORD

Run `./build.sh`. Partial signing configuration fails; no signing variables means an explicitly unsigned release. Preview builds always use the development certificate. Do not distribute an unsigned APK as installable software or use a development certificate as the stable production identity.

Before publishing, complete the relevant physical-device checks, bump the monotonically increasing versionCode, verify the signing certificate and APK checksum, and attach only the intended APK and SHA256SUMS. Community preview releases must identify untested hardware/firmware behavior and contain no performance claims without measurements.

Source ZIPs exclude Git metadata, local SDK configuration, keys and build caches. Use a fresh source commit history for this repository. Preserve the MIT application notice and build-tool notices.
