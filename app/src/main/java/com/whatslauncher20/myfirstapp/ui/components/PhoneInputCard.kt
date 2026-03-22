package com.whatslauncher20.myfirstapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.whatslauncher20.myfirstapp.util.COUNTRY_CODES
import com.whatslauncher20.myfirstapp.util.Country
import com.whatslauncher20.myfirstapp.util.findCountryByCode
import com.whatslauncher20.myfirstapp.util.getPhoneLength

@Composable
fun PhoneInputCard(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    phoneError: String?,
    selectedCode: String,
    onCodeChange: (String) -> Unit,
    codeExpanded: Boolean,
    onCodeExpandedChange: (Boolean) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit,
    onClearFocus: () -> Unit
) {
    val selectedCountry = findCountryByCode(selectedCode)
    val displayCode = if (selectedCountry != null) {
        "${selectedCountry.flag} ${selectedCountry.code}"
    } else {
        selectedCode
    }
    val maxLength = getPhoneLength(selectedCode).last

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PHONE NUMBER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Country code picker
                OutlinedTextField(
                    value = displayCode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Code") },
                    modifier = Modifier
                        .width(100.dp)
                        .clickable { onCodeExpandedChange(true) },
                    singleLine = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (codeExpanded) {
                    CountryPickerDialog(
                        onDismiss = { onCodeExpandedChange(false) },
                        onSelect = { country ->
                            onCodeChange(country.code)
                            onCodeExpandedChange(false)
                        }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Phone number field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } &&
                            (newValue.length <= maxLength || newValue.length < phoneNumber.length)
                        ) {
                            onPhoneChange(newValue)
                        }
                    },
                    label = { Text("Number") },
                    isError = phoneError != null,
                    trailingIcon = {
                        if (phoneNumber.isNotEmpty()) {
                            IconButton(onClick = { onPhoneChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    supportingText = {
                        if (phoneError != null) {
                            Text(phoneError)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onClearFocus() }
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message field
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Message") },
                placeholder = { Text("Optional") },
                trailingIcon = {
                    if (message.isNotEmpty()) {
                        IconButton(onClick = { onMessageChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onClearFocus() }
                ),
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CountryPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (Country) -> Unit
) {
    var search by remember { mutableStateOf("") }

    val filtered = remember(search) {
        if (search.isBlank()) {
            COUNTRY_CODES
        } else {
            val q = search.lowercase()
            COUNTRY_CODES.filter {
                it.name.lowercase().contains(q) || it.code.contains(q)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Text(
                    text = "Select Country",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
                )

                // Search bar
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search country or code...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (search.isNotEmpty()) {
                            IconButton(onClick = { search = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Divider()

                // Country list
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(country) }
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = country.flag, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = country.name,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = country.code,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
