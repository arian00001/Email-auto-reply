package site.arian00001.devstyleautoreply

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.search.FlagTerm

data class InboxItem(
    val sender: String,
    val subject: String,
    val date: String,
    val preview: String
)

class GmailService(private val context: Context) {
    private val store = SecureStore(context)

    private fun mailSession(): Session {
        val props = Properties().apply {
            put("mail.imap.ssl.enable", "true")
            put("mail.imap.host", "imap.gmail.com")
            put("mail.imap.port", "993")
            put("mail.imap.connectiontimeout", "15000")
            put("mail.imap.timeout", "15000")
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "465")
            put("mail.smtp.auth", "true")
            put("mail.smtp.connectiontimeout", "15000")
            put("mail.smtp.timeout", "15000")
        }
        return Session.getInstance(props)
    }

    private fun connectImap(): Store {
        check(store.hasCredentials()) { "Add your Gmail address and App Password in Settings." }
        return mailSession().getStore("imap").apply {
            connect("imap.gmail.com", store.email, store.appPassword)
        }
    }

    suspend fun testConnection(): String = withContext(Dispatchers.IO) {
        val connection = connectImap()
        try {
            val inbox = connection.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)
            val count = inbox.messageCount
            inbox.close(false)
            "Connected successfully. Inbox currently has $count messages."
        } finally {
            connection.close()
        }
    }

    suspend fun loadInbox(limit: Int = 30): List<InboxItem> = withContext(Dispatchers.IO) {
        val connection = connectImap()
        try {
            val inbox = connection.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)
            val start = (inbox.messageCount - limit + 1).coerceAtLeast(1)
            if (inbox.messageCount == 0) return@withContext emptyList()
            inbox.getMessages(start, inbox.messageCount).reversed().map { msg ->
                val from = (msg.from?.firstOrNull() as? InternetAddress)
                val sender = from?.let { listOfNotNull(it.personal, it.address).joinToString(" · ") }
                    ?: (msg.from?.joinToString() ?: "Unknown sender")
                InboxItem(
                    sender = sender,
                    subject = msg.subject ?: "(No subject)",
                    date = msg.receivedDate?.toString() ?: msg.sentDate?.toString() ?: "",
                    preview = safePreview(msg)
                )
            }.also { inbox.close(false) }
        } finally {
            connection.close()
        }
    }

    private fun safePreview(message: Message): String {
        return try {
            val content = message.content
            val raw = when (content) {
                is String -> content
                else -> "(Email content preview unavailable)"
            }
            raw.replace(Regex("\\s+"), " ").take(140)
        } catch (_: Exception) {
            "(Email content preview unavailable)"
        }
    }

    suspend fun processUnread(): Int = withContext(Dispatchers.IO) {
        if (!store.enabled || !store.hasCredentials()) return@withContext 0
        val imap = connectImap()
        var sent = 0
        try {
            val inbox = imap.getFolder("INBOX")
            inbox.open(Folder.READ_WRITE)
            val unseen = inbox.search(FlagTerm(Flags(Flags.Flag.SEEN), false))
            // Bound each pass so one run cannot keep the device busy for too long.
            for (msg in unseen.takeLast(25)) {
                try {
                    if (shouldSkip(msg)) {
                        msg.setFlag(Flags.Flag.SEEN, true)
                        continue
                    }
                    sendReply(msg)
                    // Mark seen only after successful SMTP send.
                    msg.setFlag(Flags.Flag.SEEN, true)
                    sent++
                } catch (_: Exception) {
                    // Leave the message unread so a later run can retry.
                }
            }
            inbox.close(true)
        } finally {
            imap.close()
        }
        sent
    }

    private fun shouldSkip(msg: Message): Boolean {
        val from = msg.from?.firstOrNull()?.let { it as? InternetAddress }?.address
            ?.trim()?.lowercase() ?: return true
        if (from == store.email.lowercase()) return true
        if (from.contains("no-reply") || from.contains("noreply") || from.contains("do-not-reply")) return true
        if (msg.getHeader("Auto-Submitted")?.any { !it.equals("no", ignoreCase = true) } == true) return true
        if (msg.getHeader("List-Id") != null || msg.getHeader("Precedence")?.any {
                it.equals("bulk", true) || it.equals("list", true) || it.equals("junk", true)
            } == true) return true
        return false
    }

    private fun sendReply(original: Message) {
        val reply = MimeMessage(mailSession())
        reply.setFrom(InternetAddress(store.email))
        val sender = original.from?.firstOrNull() ?: return
        reply.setRecipient(Message.RecipientType.TO, sender)
        val originalSubject = original.subject.orEmpty()
        reply.subject = if (originalSubject.startsWith("Re:", true)) originalSubject else "Re: $originalSubject"
        reply.setText(store.replyText, "UTF-8")
        original.getHeader("Message-ID")?.firstOrNull()?.let { reply.setHeader("In-Reply-To", it) }
        original.getHeader("Message-ID")?.firstOrNull()?.let { reply.setHeader("References", it) }
        val props = Properties().apply {
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "465")
            put("mail.smtp.auth", "true")
            put("mail.smtp.connectiontimeout", "15000")
            put("mail.smtp.timeout", "15000")
        }
        val smtpSession = Session.getInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication() =
                javax.mail.PasswordAuthentication(store.email, store.appPassword)
        })
        val outbound = MimeMessage(smtpSession)
        outbound.setFrom(InternetAddress(store.email))
        outbound.setRecipient(Message.RecipientType.TO, sender)
        outbound.subject = reply.subject
        outbound.setText(store.replyText, "UTF-8")
        original.getHeader("Message-ID")?.firstOrNull()?.let { outbound.setHeader("In-Reply-To", it) }
        original.getHeader("Message-ID")?.firstOrNull()?.let { outbound.setHeader("References", it) }
        javax.mail.Transport.send(outbound)
    }
}
