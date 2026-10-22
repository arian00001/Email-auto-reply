package site.arian00001.devstyleautoreply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Lime = Color(0xFFB7F34A)
private val DeepGreen = Color(0xFF244B21)
private val Ink = Color(0xFF183018)
private val Muted = Color(0xFF657260)
private val Pale = Color(0xFFF7FBEF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContext.context = applicationContext
        val store = SecureStore(applicationContext)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = DeepGreen,
                    onPrimary = Color.White,
                    secondary = Lime,
                    background = Pale,
                    surface = Color.White,
                    onSurface = Ink
                )
            ) {
                AutoReplyApp(store)
            }
        }
    }
}

@Composable
private fun AutoReplyApp(store: SecureStore) {
    var tab by remember { mutableStateOf(0) }
    var email by remember { mutableStateOf(store.email) }
    var password by remember { mutableStateOf(store.appPassword) }
    var reply by remember { mutableStateOf(store.replyText) }
    var enabled by remember { mutableStateOf(store.enabled) }
    var inbox by remember { mutableStateOf<List<InboxItem>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Connect Gmail to get started.") }
    val scope = rememberCoroutineScope()

    fun refreshInbox() {
        scope.launch {
            busy = true
            try {
                inbox = GmailServiceFrom(store).loadInbox()
                status = "Inbox refreshed."
            } catch (e: Exception) {
                status = "Could not load inbox. Check credentials and internet."
            } finally { busy = false }
        }
    }

    LaunchedEffect(Unit) {
        if (store.hasCredentials()) refreshInbox()
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFF9FCEF), Color(0xFFECF8D8), Color.White))
        )
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).background(Lime, RoundedCornerShape(17.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MarkEmailRead, null, tint = DeepGreen, modifier = Modifier.size(27.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("DEV STYLE", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink)
                    Text("AUTO REPLY", fontSize = 11.sp, letterSpacing = 2.sp, color = Muted)
                }
                Surface(shape = RoundedCornerShape(50), color = if (enabled) Lime else Color.White) {
                    Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(if (enabled) DeepGreen else Muted, RoundedCornerShape(50)))
                        Spacer(Modifier.width(6.dp))
                        Text(if (enabled) "ACTIVE" else "PAUSED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Row(
                Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White, RoundedCornerShape(20.dp)).padding(5.dp)
            ) {
                TabPill("Home", tab == 0, Modifier.weight(1f)) { tab = 0 }
                TabPill("Settings", tab == 1, Modifier.weight(1f)) { tab = 1 }
            }
            Spacer(Modifier.height(16.dp))

            if (tab == 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Your inbox", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(email.ifBlank { "Gmail not connected" }, fontSize = 12.sp, color = Muted)
                    }
                    FilledTonalIconButton(onClick = { refreshInbox() }, enabled = !busy) {
                        Icon(Icons.Default.Refresh, "Refresh inbox")
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("INBOX", inbox.size.toString(), Modifier.weight(1f))
                    StatCard("AUTO REPLY", if (enabled) "ON" else "OFF", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Text(status, fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(8.dp))
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = DeepGreen, trackColor = Lime.copy(alpha = .35f))
                if (inbox.isEmpty() && !busy) {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.MarkEmailUnread, null, tint = DeepGreen, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Your inbox will appear here", fontWeight = FontWeight.SemiBold, color = Ink)
                        Text("Connect your Gmail in Settings, then refresh.", fontSize = 12.sp, color = Muted)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { tab = 1 }, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)) {
                            Text("Open Settings")
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                        items(inbox) { item ->
                            GlassCard(Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(38.dp).background(Lime.copy(alpha = .6f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Email, null, tint = DeepGreen)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(item.sender, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1)
                                        Text(item.subject, fontSize = 12.sp, color = DeepGreen, maxLines = 1)
                                    }
                                }
                                if (item.preview.isNotBlank()) {
                                    Spacer(Modifier.height(7.dp))
                                    Text(item.preview, fontSize = 12.sp, color = Muted, maxLines = 2)
                                }
                                if (item.date.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(item.date, fontSize = 10.sp, color = Muted, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    item {
                        Text("Mail settings", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Securely connect your Gmail account", fontSize = 12.sp, color = Muted)
                    }
                    item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Text("GMAIL CONNECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = DeepGreen)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = email, onValueChange = { email = it },
                                label = { Text("Gmail address") }, singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = password, onValueChange = { password = it.filterNot(Char::isWhitespace) },
                                label = { Text("16-character App Password") }, singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Use a Google App Password, not your normal Gmail password. 2-Step Verification must be enabled.", fontSize = 11.sp, color = Muted)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    store.email = email
                                    store.appPassword = password
                                    scope.launch {
                                        busy = true
                                        status = try { GmailServiceFrom(store).testConnection() }
                                        catch (e: Exception) { "Connection failed. Verify Gmail, App Password, and IMAP access." }
                                        busy = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
                            ) { Icon(Icons.Default.Link, null); Spacer(Modifier.width(8.dp)); Text("Save & test connection") }
                            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = DeepGreen)
                            Text(status, fontSize = 12.sp, color = Muted)
                        }
                    }
                    item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Text("AUTO-REPLY", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = DeepGreen)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Automatically reply to new unread email", fontWeight = FontWeight.SemiBold, color = Ink)
                                    Text("Checks about every 15 minutes; Android may delay background work.", fontSize = 11.sp, color = Muted)
                                }
                                Switch(checked = enabled, onCheckedChange = {
                                    enabled = it
                                    store.enabled = it
                                    AutoReplyScheduler.update(applicationContextFor(store), it)
                                    if (it) AutoReplyScheduler.runNow(applicationContextFor(store))
                                }, colors = SwitchDefaults.colors(checkedThumbColor = DeepGreen, checkedTrackColor = Lime))
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = reply, onValueChange = { reply = it },
                                label = { Text("Reply message") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 190.dp),
                                minLines = 7
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    store.replyText = reply
                                    status = "Reply message saved."
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
                            ) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Save reply message") }
                        }
                    }
                    item {
                        Text("Safety: auto-reply skips your own address, common no-reply senders, and mailing-list/bulk messages. Review your message before enabling.", fontSize = 11.sp, color = Muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = if (selected) Lime else Color.Transparent
    ) {
        Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = Ink)
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Text(title, fontSize = 10.sp, letterSpacing = 1.sp, color = Muted, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 23.sp, color = Ink, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.background(Color.White.copy(alpha = 0.78f), RoundedCornerShape(22.dp))
            .border(1.dp, Color.White.copy(alpha = .95f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        content = content
    )
}

// Tiny adapters keep UI callbacks straightforward.
private fun GmailServiceFrom(store: SecureStore): GmailService = GmailService(storeContext(store))
private fun applicationContextFor(store: SecureStore): android.content.Context {
    // SecureStore is constructed with application context by the activity; use a dedicated context
    // through the application-level registry below.
    return AppContext.context ?: throw IllegalStateException("App context unavailable")
}

private object AppContext {
    var context: android.content.Context? = null
}
