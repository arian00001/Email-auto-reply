package site.arian00001.devstyleautoreply

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_mail_settings",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var email: String
        get() = prefs.getString("email", "") ?: ""
        set(value) { prefs.edit().putString("email", value.trim()).apply() }

    var appPassword: String
        get() = prefs.getString("app_password", "") ?: ""
        set(value) { prefs.edit().putString("app_password", value.replace(" ", "").trim()).apply() }

    var enabled: Boolean
        get() = prefs.getBoolean("auto_reply_enabled", false)
        set(value) { prefs.edit().putBoolean("auto_reply_enabled", value).apply() }

    var replyText: String
        get() = prefs.getString("reply_text", DEFAULT_REPLY) ?: DEFAULT_REPLY
        set(value) { prefs.edit().putString("reply_text", value).apply() }


    var imgbbApiKey: String
        get() = prefs.getString("imgbb_api_key", "") ?: ""
        set(value) { prefs.edit().putString("imgbb_api_key", value.trim()).apply() }

    fun hasCredentials() = email.isNotBlank() && appPassword.isNotBlank()

    companion object {
        const val DEFAULT_REPLY = """Hello,

Thank you for reaching out to DEV STYLE!

We have received your email, and we appreciate your interest and support. Your message is important to us.

Our team will review your message and get back to you as soon as possible if a response is required.

Thank you for being a part of DEV STYLE. We appreciate your patience and support!

Best regards,
DEV STYLE Team
Website: https://arian00001.site
Telegram: @DevStyleHQ"""
    }
}
