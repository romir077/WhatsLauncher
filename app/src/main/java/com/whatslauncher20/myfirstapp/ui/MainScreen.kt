package com.whatslauncher20.myfirstapp.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatslauncher20.myfirstapp.ui.components.ClipboardBanner
import com.whatslauncher20.myfirstapp.ui.components.FavoritesSection
import com.whatslauncher20.myfirstapp.ui.components.MessageTemplatesSection
import com.whatslauncher20.myfirstapp.ui.components.PhoneInputCard
import com.whatslauncher20.myfirstapp.ui.components.RecentNumbersSection
import com.whatslauncher20.myfirstapp.ui.theme.*
import com.whatslauncher20.myfirstapp.util.*
import kotlinx.coroutines.launch

data class MessengerApp(
    val name: String,
    val color: Color,
    val contentColor: Color = Color.White,
    val launch: (Context, String, String, String) -> Unit
)

private val messengerApps = listOf(
    MessengerApp("WhatsApp", WhatsAppGreen) { ctx, code, phone, msg ->
        openWhatsApp(ctx, "${code}${phone}", msg)
    },
    MessengerApp("Telegram", TelegramBlue) { ctx, code, phone, msg ->
        openTelegram(ctx, "${code}${phone}", msg)
    },
    MessengerApp("Signal", SignalBlue) { ctx, code, phone, msg ->
        openSignal(ctx, "${code}${phone}", msg)
    },
    MessengerApp("Arattai", ArattaiYellow, contentColor = Color(0xFF3E2723)) { ctx, code, phone, msg ->
        openArattai(ctx, code, phone, msg)
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(sharedPhoneNumber: String? = null) {
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCode by remember { mutableStateOf("+91") }
    var codeExpanded by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var clipboardNumber by remember { mutableStateOf<String?>(null) }

    // Dialog state
    var showAddTemplateDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<String?>(null) }
    var showFavLabelDialog by remember { mutableStateOf<FavLabelDialogState?>(null) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val recentNumbers = remember { mutableStateListOf<String>() }
    val favorites = remember { mutableStateListOf<Favorite>() }
    val templates = remember { mutableStateListOf<String>() }

    fun refreshRecents() {
        recentNumbers.clear()
        recentNumbers.addAll(loadRecentNumbers(context))
    }

    fun refreshFavorites() {
        favorites.clear()
        favorites.addAll(loadFavorites(context))
    }

    fun refreshTemplates() {
        templates.clear()
        templates.addAll(loadTemplates(context))
    }

    val currentSelectedCode by rememberUpdatedState(selectedCode)

    fun updateClipboard() {
        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = sysClipboard.primaryClip
        // On Android 10+, clipboard may be null when app lacks focus — keep existing value
        if (clip == null || clip.itemCount == 0) return
        val clipText = clip.getItemAt(0)?.text?.toString() ?: ""
        val extracted = extractPhoneNumber(clipText)
        clipboardNumber = if (extracted != null) extractPhoneWithoutCode(clipText, currentSelectedCode) else null
    }

    LaunchedEffect(Unit) {
        refreshRecents()
        refreshFavorites()
        refreshTemplates()
        updateClipboard()
    }

    // Real-time clipboard listener
    DisposableEffect(Unit) {
        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val listener = ClipboardManager.OnPrimaryClipChangedListener { updateClipboard() }
        sysClipboard.addPrimaryClipChangedListener(listener)
        onDispose { sysClipboard.removePrimaryClipChangedListener(listener) }
    }

    // Refresh clipboard when app comes back to foreground or starts
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                updateClipboard()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(sharedPhoneNumber) {
        if (sharedPhoneNumber != null) {
            val extracted = extractPhoneNumber(sharedPhoneNumber)
            if (extracted != null) {
                phoneNumber = extractPhoneWithoutCode(sharedPhoneNumber, selectedCode)
                phoneError = null
            }
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsLauncher", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WhatsAppTeal,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Privacy Policy") },
                            onClick = {
                                menuExpanded = false
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://romir077.github.io/WhatsLauncher/privacy-policy.html"))
                                context.startActivity(intent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Report a safety concern") },
                            onClick = {
                                menuExpanded = false
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:ruchir.mehta2112@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "WhatsLauncher - Safety Concern Report")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                text = "Open a chat on any messenger\nwithout adding them to your contacts",
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
                }
            )

            PhoneInputCard(
                phoneNumber = phoneNumber,
                onPhoneChange = { phoneNumber = it; phoneError = null },
                phoneError = phoneError,
                selectedCode = selectedCode,
                onCodeChange = { newCode ->
                    selectedCode = newCode
                    val newMax = getPhoneLength(newCode).last
                    if (phoneNumber.length > newMax) {
                        phoneNumber = phoneNumber.takeLast(newMax)
                    }
                    phoneError = null
                    updateClipboard()
                },
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
                messengerApps.forEach { app ->
                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            launchApp(
                                context, phoneNumber, selectedCode, message,
                                focusManager, recentNumbers, app.name, snackbarHostState, scope,
                                { code, phone, msg -> app.launch(context, code, phone, msg) }
                            ) { phoneError = it }
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = app.color,
                            contentColor = app.contentColor
                        ),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(app.name, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }

            MessageTemplatesSection(
                templates = templates,
                onTemplateSelected = { message = it },
                onAddTemplate = { showAddTemplateDialog = true },
                onEditTemplate = { editingTemplate = it },
                onDeleteTemplate = { template ->
                    removeTemplate(context, template)
                    refreshTemplates()
                }
            )

            FavoritesSection(
                favorites = favorites,
                onNumberSelected = { code, phone ->
                    selectedCode = code
                    phoneNumber = phone
                    phoneError = null
                },
                onEditLabel = { fav ->
                    showFavLabelDialog = FavLabelDialogState(fav.number, fav.label)
                },
                onRemoveFavorite = { number ->
                    removeFavorite(context, number)
                    refreshFavorites()
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
                    refreshRecents()
                },
                onClear = {
                    clearRecentNumbers(context)
                    recentNumbers.clear()
                },
                onFavorite = { number ->
                    showFavLabelDialog = FavLabelDialogState(number, "")
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

    // Add template dialog
    if (showAddTemplateDialog) {
        TemplateDialog(
            title = "New Message Template",
            initialText = "",
            onDismiss = { showAddTemplateDialog = false },
            onSave = { template ->
                saveTemplate(context, template)
                refreshTemplates()
                showAddTemplateDialog = false
            }
        )
    }

    // Edit template dialog
    editingTemplate?.let { oldTemplate ->
        TemplateDialog(
            title = "Edit Template",
            initialText = oldTemplate,
            onDismiss = { editingTemplate = null },
            onSave = { newTemplate ->
                editTemplate(context, oldTemplate, newTemplate)
                refreshTemplates()
                editingTemplate = null
            }
        )
    }

    // Favorite label dialog
    showFavLabelDialog?.let { state ->
        FavoriteLabelDialog(
            number = state.number,
            initialLabel = state.label,
            onDismiss = { showFavLabelDialog = null },
            onSave = { label ->
                if (state.label.isEmpty() && loadFavorites(context).none { it.number == state.number }) {
                    addFavorite(context, state.number, label)
                } else {
                    updateFavoriteLabel(context, state.number, label)
                }
                refreshFavorites()
                showFavLabelDialog = null
            }
        )
    }
}

private data class FavLabelDialogState(val number: String, val label: String)

private fun launchApp(
    context: Context,
    phoneNumber: String,
    selectedCode: String,
    message: String,
    focusManager: FocusManager,
    recentNumbers: SnapshotStateList<String>,
    appName: String,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    openApp: (code: String, phone: String, msg: String) -> Unit,
    setError: (String?) -> Unit
) {
    val error = validatePhone(phoneNumber, selectedCode)
    if (error != null) {
        setError(error)
    } else {
        setError(null)
        focusManager.clearFocus()
        val code = selectedCode.replace("+", "")
        saveRecentNumber(context, "$selectedCode $phoneNumber")
        recentNumbers.clear()
        recentNumbers.addAll(loadRecentNumbers(context))
        openApp(code, phoneNumber, message.trim())
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = "Opened in $appName",
                duration = SnackbarDuration.Short
            )
        }
    }
}

@Composable
private fun TemplateDialog(
    title: String,
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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

@Composable
private fun FavoriteLabelDialog(
    number: String,
    initialLabel: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var label by remember { mutableStateOf(initialLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Label for Favorite") },
        text = {
            Column {
                Text(
                    text = number,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    placeholder = { Text("e.g. Mom, Plumber, Doctor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(label.trim()) }) {
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
