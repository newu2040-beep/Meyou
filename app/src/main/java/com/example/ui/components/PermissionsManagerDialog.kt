package com.example.ui.components

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasNotif by remember { mutableStateOf(PermissionHelper.hasNotificationPermission(context)) }
    var hasGallery by remember { mutableStateOf(PermissionHelper.hasGalleryAndStoragePermission(context)) }
    var hasFullFiles by remember { mutableStateOf(PermissionHelper.hasFullFileAccess()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasNotif = PermissionHelper.hasNotificationPermission(context)
        hasGallery = PermissionHelper.hasGalleryAndStoragePermission(context)
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "App Permissions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enable notifications, gallery & files access",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Permission 1: Notifications
            PermissionItemCard(
                title = "Notification Permission",
                description = "For reading reminders, streak alerts and background TTS player status",
                isGranted = hasNotif,
                icon = Icons.Outlined.Notifications,
                onRequest = {
                    val permissions = PermissionHelper.getRequiredPermissions().toTypedArray()
                    permissionLauncher.launch(permissions)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Permission 2: Gallery & Media Photos
            PermissionItemCard(
                title = "Gallery & Photos Access",
                description = "To customize book covers and album art with your personal photos",
                isGranted = hasGallery,
                icon = Icons.Outlined.PhotoLibrary,
                onRequest = {
                    val permissions = PermissionHelper.getRequiredPermissions().toTypedArray()
                    permissionLauncher.launch(permissions)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Permission 3: Files & Storage Access
            PermissionItemCard(
                title = "Files & Storage Access",
                description = "To import eBooks, export PDF/TXT/CSV/JSON files and manage cache",
                isGranted = hasFullFiles || hasGallery,
                icon = Icons.Outlined.FolderShared,
                onRequest = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        PermissionHelper.openFullStorageSettings(context)
                    } else {
                        val permissions = PermissionHelper.getRequiredPermissions().toTypedArray()
                        permissionLauncher.launch(permissions)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grant All Permissions Button
            Button(
                onClick = {
                    val permissions = PermissionHelper.getRequiredPermissions().toTypedArray()
                    permissionLauncher.launch(permissions)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("grant_all_permissions_button")
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant All Permissions", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    onRequest: () -> Unit
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
                        if (isGranted) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "Granted",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                FilledTonalButton(
                    onClick = onRequest,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Allow", fontSize = 12.sp)
                }
            }
        }
    }
}
