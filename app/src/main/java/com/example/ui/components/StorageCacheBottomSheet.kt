package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.StorageCacheHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageCacheBottomSheet(
    onRemoveSampleData: () -> Unit,
    onRestoreSampleData: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var cacheSizeBytes by remember { mutableLongStateOf(StorageCacheHelper.getCacheSizeBytes(context)) }
    var databaseSizeBytes by remember { mutableLongStateOf(StorageCacheHelper.getDatabaseSizeBytes(context)) }
    var showConfirmDeleteSample by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Storage & Cache",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Manage local device storage, cache & sample data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Storage Overview Cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StorageMetricCard(
                    title = "Recent Cache",
                    size = StorageCacheHelper.formatBytes(cacheSizeBytes),
                    icon = Icons.Outlined.CleaningServices,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StorageMetricCard(
                    title = "Database & Books",
                    size = StorageCacheHelper.formatBytes(databaseSizeBytes),
                    icon = Icons.Outlined.FolderOpen,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CLEANUP ACTIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action 1: Clear Recent Cache
            StorageActionTile(
                title = "Remove Recent Cache",
                subtitle = "Deletes temporary exported files, page renderings and image caches",
                icon = Icons.Default.DeleteSweep,
                buttonText = "Clear Cache",
                isDestructive = false,
                onClick = {
                    val success = StorageCacheHelper.clearRecentCache(context)
                    cacheSizeBytes = StorageCacheHelper.getCacheSizeBytes(context)
                    if (success) {
                        Toast.makeText(context, "Cache successfully cleared!", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action 2: Remove Sample Data
            StorageActionTile(
                title = "Remove Sample Data",
                subtitle = "Deletes preloaded demo books and articles to keep only your own content",
                icon = Icons.Outlined.AutoStories,
                buttonText = "Remove Samples",
                isDestructive = true,
                onClick = {
                    showConfirmDeleteSample = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action 3: Restore Sample Data
            StorageActionTile(
                title = "Restore Sample Library",
                subtitle = "Re-seed standard classic eBooks and curated articles",
                icon = Icons.Outlined.Restore,
                buttonText = "Restore Samples",
                isDestructive = false,
                onClick = {
                    onRestoreSampleData()
                    databaseSizeBytes = StorageCacheHelper.getDatabaseSizeBytes(context)
                    Toast.makeText(context, "Sample library restored!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showConfirmDeleteSample) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteSample = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove Sample Data?") },
            text = {
                Text("This will remove the default sample books and articles. Any books or articles you imported yourself will be safely preserved.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveSampleData()
                        databaseSizeBytes = StorageCacheHelper.getDatabaseSizeBytes(context)
                        showConfirmDeleteSample = false
                        Toast.makeText(context, "Sample data removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteSample = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StorageMetricCard(
    title: String,
    size: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = size,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StorageActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    buttonText: String,
    isDestructive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDestructive) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isDestructive) {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(buttonText, fontSize = 12.sp)
                }
            } else {
                FilledTonalButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(buttonText, fontSize = 12.sp)
                }
            }
        }
    }
}
