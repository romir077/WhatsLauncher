package com.whatslauncher20.myfirstapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentNumbersSection(
    recentNumbers: List<String>,
    onNumberSelected: (code: String, phone: String) -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    onFavorite: (String) -> Unit
) {
    if (recentNumbers.isEmpty()) return

    Spacer(modifier = Modifier.height(20.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Recent",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = onClear,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text("Clear all", fontSize = 12.sp)
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    recentNumbers.forEach { number ->
        Card(
            onClick = {
                val parts = number.split(" ", limit = 2)
                if (parts.size == 2) {
                    onNumberSelected(parts[0], parts[1])
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = number,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onFavorite(number) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.StarOutline,
                        contentDescription = "Add to favorites",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { onRemove(number) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}
