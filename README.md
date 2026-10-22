# DEV STYLE Auto Reply (Android / Kotlin)

A native Android Studio starter app with Home + Settings, Gmail inbox view, Gmail App Password authentication, customizable auto-reply, and GitHub Actions APK builds.

## Important behavior and limits
- Uses Gmail IMAP (`imap.gmail.com:993`) to read inbox and SMTP SSL (`smtp.gmail.com:465`) to send replies.
- Background checks use WorkManager's periodic schedule (minimum interval 15 minutes). Android can delay work due to Doze, battery saver, force-stop, no network, or OEM background restrictions. This is **not guaranteed instant 24/7 delivery**.
- If you need reliable always-on replies, use Gmail's server-side vacation responder or a continuously running backend with OAuth/Gmail API.
- The app skips messages from the account itself, common no-reply addresses, and mailing-list/bulk messages. It processes up to 25 unread messages per run and marks them read after successful reply (or if intentionally skipped).
- Do not use this for unsolicited bulk email. Gmail sending limits and anti-abuse policies apply.
- Credentials are stored using AndroidX EncryptedSharedPreferences, but device compromise/rooting can still increase risk. Never share the APK containing your credentials; credentials are entered on-device only.
- No credential, API key, or app password is committed to the repository.

## Gmail setup
1. Turn on Google 2-Step Verification.
2. Create a Google App Password at https://myaccount.google.com/apppasswords. Google may not show this option for some managed/workspace accounts or Advanced Protection accounts.
3. In the app's Settings, enter the Gmail address and the 16-character App Password, then tap **Save & test connection**.
4. Customize and save the reply message.
5. Enable Auto-Reply. It checks on a periodic schedule and may be delayed by Android.

If login fails, confirm the address and App Password are correct and that IMAP is available for your account. Do not enter your normal Google password.

## Build APK using GitHub Actions
1. Create a **private** GitHub repository and upload all files in this project.
2. Open **Actions** and enable workflows if GitHub asks.
3. Run the `Android APK Build` workflow, or push a commit to `main` / `master`.
4. Download the `dev-style-auto-reply-apk` artifact from the workflow run. The debug APK is suitable for testing.
5. For a public release, configure signing securely using a release keystore and GitHub Secrets; do not commit keystores or passwords.

## Build locally
Open the folder in Android Studio (JDK 17), allow Gradle sync, then run `./gradlew assembleDebug`.
