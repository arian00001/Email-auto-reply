<div align="center">

# 3MA1L
### Gmail + Gemini AI Auto Reply
**Kotlin · IMAP/SMTP · Gemini API · Foreground service**

</div>

## Implemented in source
- Connects to Gmail over IMAPS (`imap.gmail.com:993`) and sends HTML email over SMTP SSL (`smtp.gmail.com:465`) using a Gmail App Password.
- Lists inbox messages in pages of 50 with Load More; no fixed 30-message cap.
- Calls Gemini `generateContent` over HTTPS with the configured API key/model.
- Optional foreground service polls unread inbox messages on a 30-second target loop and sends a generated HTML reply when auto-reply is enabled.
- Includes your logo asset and a white/lime UI.
- GitHub Actions workflow builds a debug APK.

## Setup
1. Open in Android Studio using JDK 17 and sync Gradle.
2. Build the debug APK.
3. Turn on Google 2-Step Verification and create a Gmail App Password if available for your account.
4. In 3MA1L, enter Gmail address, App Password, Gemini API key, and model (`gemini-2.5-flash` default).
5. Test with a secondary email account first. Save settings, enable auto-reply, then tap Start monitor.
6. Inspect the persistent notification for connection errors.

## Real-world limitations
- **Not build-verified here:** run GitHub Actions/Android Studio build and fix any environment-specific issue before use.
- Android battery optimisations and Doze can delay background polling; exact 30-second 24/7 delivery cannot be guaranteed.
- Some Google accounts block IMAP/App Password. If so, Gmail OAuth 2.0/Gmail API is needed.
- This sends replies automatically to unread messages when enabled. Use a test account first and be aware it can reply to newsletters or automated senders.
- Gemini output is sent as HTML returned by the model. Do not use for sensitive messages without human review.
- Credentials are stored in app preferences in this starter, not encrypted. Do not distribute this build publicly until encrypted storage, rate limits, sender allowlists, duplicate prevention, and HTML sanitisation are added.
- Never commit credentials or API keys to GitHub.

## Build with GitHub Actions
Push to `main` or `master`, or run the `Android Build` workflow manually. The debug APK is uploaded as an artifact.
