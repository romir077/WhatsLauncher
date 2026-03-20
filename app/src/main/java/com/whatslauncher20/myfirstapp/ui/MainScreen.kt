package com.whatslauncher20.myfirstapp.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatslauncher20.myfirstapp.ui.components.ClipboardBanner
import com.whatslauncher20.myfirstapp.ui.components.FavoritesSection
import com.whatslauncher20.myfirstapp.ui.components.MessageTemplatesSection
import com.whatslauncher20.myfirstapp.ui.components.PhoneInputCard
import com.whatslauncher20.myfirstapp.ui.components.RecentNumbersSection
import com.whatslauncher20.myfirstapp.ui.theme.ArattaiYellow
import com.whatslauncher20.myfirstapp.ui.theme.SignalBlue
import com.whatslauncher20.myfirstapp.ui.theme.TelegramBlue
import com.whatslauncher20.myfirstapp.ui.theme.WhatsAppGreen
import com.whatslauncher20.myfirstapp.ui.theme.WhatsAppTeal
import com.whatslauncher20.myfirstapp.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCode by remember { mutableStateOf("+91") }
    var codeExpanded by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var clipboardNumber by remember { mutableStateOf<String?>(null) }
    var showAddTemplateDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val recentNumbers = remember { mutableStateListOf<String>() }
    val favorites = remember { mutableStateListOf<String>() }
    val templates = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        recentNumbers.addAll(loadRecentNumbers(context))
        favorites.addAll(loadFavorites(context))
        templates.addAll(loadTemplates(context))
        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipText = sysClipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
        val extracted = extractPhoneNumber(clipText)
        if (extracted != null) clipboardNumber = extracted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsLauncher", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WhatsAppTeal,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Chat Without Saving",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Open a WhatsApp chat with any number\nwithout adding them to your contacts",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            ClipboardBanner(
                clipboardNumber = clipboardNumber,
                onUse = { number ->
                    phoneNumber = number
                    phoneError = null
                    clipboardNumber = null
                }
            )

            PhoneInputCard(
                phoneNumber = phoneNumber,
                onPhoneChange = { phoneNumber = it; phoneError = null },
                phoneError = phoneError,
                selectedCode = selectedCode,
                onCodeChange = { selectedCode = it },
                codeExpanded = codeExpanded,
                onCodeExpandedChange = { codeExpanded = it },
                message = message,
                onMessageChange = { message = it },
                onClearFocus = { focusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Open in",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp
                Button(
                    onClick = { launchApp(context, phoneNumber, selectedCode, message, focusManager, recentNumbers, { pn, msg -> openWhatsApp(context, pn, msg) }) { phoneError = it } },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("WhatsApp", fontSize = 10.sp, maxLines = 1)
                    }
                }
                // Telegram
                Button(
                    onClick = { launchApp(context, phoneNumber, selectedCode, message, focusManager, recentNumbers, { pn, msg -> openTelegram(context, pn, msg) }) { phoneError = it } },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Telegram", fontSize = 10.sp, maxLines = 1)
                    }
                }
                // Signal
                Button(
                    onClick = { launchApp(context, phoneNumber, selectedCode, message, focusManager, recentNumbers, { pn, msg -> openSignal(context, pn, msg) }) { phoneError = it } },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SignalBlue),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Signal", fontSize = 10.sp, maxLines = 1)
                    }
                }
                // Arattai
                Button(
                    onClick = { launchApp(context, phoneNumber, selectedCode, message, focusManager, recentNumbers, { _, msg -> openArattai(context, selectedCode.replace("+", ""), phoneNumber, msg) }) { phoneError = it } },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArattaiYellow, contentColor = Color(0xFF3E2723)),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Arattai", fontSize = 10.sp, maxLines = 1)
                    }
                }
            }

            MessageTemplatesSection(
                templates = templates,
                onTemplateSelected = { message = it },
                onAddTemplate = { showAddTemplateDialog = true },
                onDeleteTemplate = { template ->
                    removeTemplate(context, template)
                    templates.clear()
                    templates.addAll(loadTemplates(context))
                }
            )

            FavoritesSection(
                favorites = favorites,
                onNumberSelected = { code, phone ->
                    selectedCode = code
                    phoneNumber = phone
                    phoneError = null
                },
                onRemoveFavorite = { number ->
                    removeFavorite(context, number)
                    favorites.clear()
                    favorites.addAll(loadFavorites(context))
                }
            )

            RecentNumbersSection(
                recentNumbers = recentNumbers,
                onNumberSelected = { code, phone ->
                    selectedCode = code
                    phoneNumber = phone
                    phoneError = null
                },
                onRemove = { number ->
                    removeRecentNumber(context, number)
                    recentNumbers.clear()
                    recentNumbers.addAll(loadRecentNumbers(context))
                },
                onClear = {
                    clearRecentNumbers(context)
                    recentNumbers.clear()
                },
                onFavorite = { number ->
                    addFavorite(context, number)
                    favorites.clear()
                    favorites.addAll(loadFavorites(context))
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Made with \u2764 by Ruchir Mehta",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }

    if (showAddTemplateDialog) {
        AddTemplateDialog(
            onDismiss = { showAddTemplateDialog = false },
            onSave = { template ->
                saveTemplate(context, template)
                templates.clear()
                templates.addAll(loadTemplates(context))
                showAddTemplateDialog = false
            }
        )
    }
}

private fun launchApp(
    context: Context,
    phoneNumber: String,
    selectedCode: String,
    message: String,
    focusManager: FocusManager,
    recentNumbers: SnapshotStateList<String>,
    openApp: (fullNumber: String, msg: String) -> Unit,
    setError: (String?) -> Unit
) {
    val error = validatePhone(phoneNumber)
    if (error != null) {
        setError(error)
    } else {
        setError(null)
        focusManager.clearFocus()
        val code = selectedCode.replace("+", "")
        saveRecentNumber(context, "$selectedCode $phoneNumber")
        recentNumbers.clear()
        recentNumbers.addAll(loadRecentNumbers(context))
        openApp("$code$phoneNumber", message.trim())
    }
}

@Composable
private fun AddTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Message Template") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("e.g. Hi, I found your number on...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onSave(text.trim()) },
                enabled = text.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
