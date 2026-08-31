# Google Play signing setup

Do this only after the v1.0.0 debug APK has been tested successfully.

Create one permanent upload key:

```bash
keytool -genkeypair -v   -keystore tra-upload-key.jks   -alias tra-upload   -keyalg RSA   -keysize 4096   -validity 10000
```

Keep `tra-upload-key.jks` permanently and never commit it to GitHub.

Create a Base64 copy:

### macOS / Linux

```bash
base64 < tra-upload-key.jks | tr -d '\n' > tra-keystore-base64.txt
```

Add these GitHub repository secrets:

- `ANDROID_KEYSTORE_BASE64` — full Base64 string
- `ANDROID_KEYSTORE_PASSWORD` — keystore password
- `ANDROID_KEY_ALIAS` — `tra-upload`
- `ANDROID_KEY_PASSWORD` — key password

Then run:

**Actions → Build Signed TRA Play Store Release**

For the first Play release use:

- Version name: `1.0.0`
- Version code: `1`

Future Play releases must use a higher version code.
