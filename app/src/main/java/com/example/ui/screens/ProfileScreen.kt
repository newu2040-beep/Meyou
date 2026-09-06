package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.datastore.*
import com.example.domain.model.ReadingStats
import com.example.ui.components.AboutMeyouDialog
import com.example.ui.components.PermissionsBottomSheet
import com.example.ui.components.StorageCacheBottomSheet
import com.example.ui.theme.*
import com.example.util.ImageHelper
import kotlinx.coroutines.launch

data class AvatarPresetItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val containerColor: Color
)

val AVATAR_PRESETS = listOf(
    AvatarPresetItem("avatar_scholar", "Scholar", Icons.AutoMirrored.Filled.MenuBook, Color(0xFF6750A4), Color(0xFFE8DEF8)),
    AvatarPresetItem("avatar_botanist", "Botanist", Icons.Default.Spa, Color(0xFF2E6B4F), Color(0xFFD6E8DC)),
    AvatarPresetItem("avatar_dreamer", "Dreamer", Icons.Default.NightsStay, Color(0xFF1B5E86), Color(0xFFCEE5FF)),
    AvatarPresetItem("avatar_essayist", "Essayist", Icons.Default.EditNote, Color(0xFF9E4822), Color(0xFFFFDBD0)),
    AvatarPresetItem("avatar_artisan", "Artisan", Icons.Default.Palette, Color(0xFF8C435A), Color(0xFFFFD9E1)),
    AvatarPresetItem("avatar_thinker", "Thinker", Icons.Default.Psychology, Color(0xFF42474E), Color(0xFFE0E2E8))
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    stats: ReadingStats,
    preferences: ReadingPreferences,
    onUpdateProfile: (name: String, nickname: String, age: Int, gender: String, avatarUri: String?, avatarPreset: String, bio: String) -> Unit,
    onUpdateThemeScheme: (AppThemeScheme) -> Unit,
    onUpdateThemeMode: (ThemeModeOption) -> Unit,
    onToggleCompactMode: (Boolean) -> Unit,
    onUpdatePaperTexture: (PaperTexture) -> Unit,
    onUpdateFont: (ReadingFont) -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenSettings: () -> Unit,
    onRemoveSampleData: () -> Unit = {},
    onRestoreSampleData: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dimensions = LocalAppDimensions.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showStorageCacheSheet by remember { mutableStateOf(false) }
    var showPermissionsSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Settings",
                        style = if (dimensions.isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensions.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.sectionSpacing),
            contentPadding = PaddingValues(bottom = if (dimensions.isCompact) 48.dp else 80.dp)
        ) {
            // 1. Profile Header Card
            item {
                ProfileHeaderCard(
                    userProfile = preferences.userProfile,
                    isCompact = dimensions.isCompact,
                    onEditClick = { showEditProfileDialog = true }
                )
            }

            // 2. Reading Activity Stats Snapshot
            item {
                Surface(
                    shape = ExpressiveCardShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStatistics() }
                        .testTag("profile_stats_card")
                ) {
                    Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.BarChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reading Activity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Details",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniItem(label = "Streak", value = "${stats.streakDays}d 🔥")
                            StatMiniItem(label = "Finished", value = "${stats.booksFinished}")
                            StatMiniItem(label = "Articles", value = "${stats.articlesRead}")
                            StatMiniItem(label = "Time", value = "${stats.totalMinutesRead}m")
                        }
                    }
                }
            }

            // 3. Compact Mode for Small Display Phones
            item {
                CompactModeCard(
                    isCompact = preferences.isCompactMode,
                    onToggle = onToggleCompactMode
                )
            }

            // 4. Dark and Light Mode Toggle Switch
            item {
                ThemeModeSelectorCard(
                    currentMode = preferences.themeMode,
                    onSelectMode = onUpdateThemeMode
                )
            }

            // 5. Material You Expressive Themes
            item {
                MaterialYouThemesCard(
                    selectedScheme = preferences.themeScheme,
                    onSelectScheme = onUpdateThemeScheme
                )
            }

            // 6. Paper Texture Effects
            item {
                PaperTexturesCard(
                    selectedTexture = preferences.paperTexture,
                    onSelectTexture = onUpdatePaperTexture
                )
            }

            // 7. Typography & Fonts
            item {
                ReaderFontsCard(
                    selectedFont = preferences.font,
                    onSelectFont = onUpdateFont,
                    onOpenMoreSettings = onOpenSettings
                )
            }

            // 8. General Actions (Offline reading, about)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "App & Info",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Security,
                        title = "App Permissions",
                        subtitle = "Notifications, Gallery photos & storage access",
                        onClick = { showPermissionsSheet = true }
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.CloudDone,
                        title = "Offline Storage & Cache",
                        subtitle = "Clean recent cache and manage sample library",
                        onClick = { showStorageCacheSheet = true }
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Info,
                        title = "About MEYOU",
                        subtitle = "Short app preview & developer credit",
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    // Profile Edit Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentProfile = preferences.userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, nickname, age, gender, uri, preset, bio ->
                onUpdateProfile(name, nickname, age, gender, uri, preset, bio)
                showEditProfileDialog = false
            }
        )
    }

    // Storage & Cache BottomSheet
    if (showStorageCacheSheet) {
        StorageCacheBottomSheet(
            onRemoveSampleData = onRemoveSampleData,
            onRestoreSampleData = onRestoreSampleData,
            onDismiss = { showStorageCacheSheet = false }
        )
    }

    // Permissions BottomSheet
    if (showPermissionsSheet) {
        PermissionsBottomSheet(
            onDismiss = { showPermissionsSheet = false }
        )
    }

    // About MEYOU Dialog with Preview & Rahul Shah credit
    if (showAboutDialog) {
        AboutMeyouDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

// ---------------------------------------------------------------------------
// 1. Profile Header Card
// ---------------------------------------------------------------------------
@Composable
fun ProfileHeaderCard(
    userProfile: UserProfile,
    isCompact: Boolean,
    onEditClick: () -> Unit
) {
    Surface(
        shape = ExpressiveHeroCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 14.dp else 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                UserAvatarView(
                    avatarUri = userProfile.avatarUri,
                    presetId = userProfile.avatarPreset,
                    size = if (isCompact) 64.dp else 84.dp
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(if (isCompact) 26.dp else 30.dp)
                        .clickable { onEditClick() }
                        .testTag("avatar_edit_badge")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change photo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Name
            Text(
                text = userProfile.name,
                style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Nickname
            Text(
                text = userProfile.nickname,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Age & Gender Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "${userProfile.age} yrs",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = userProfile.gender,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (userProfile.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userProfile.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onEditClick,
                shape = ExpressivePillShape,
                modifier = Modifier
                    .height(if (isCompact) 36.dp else 40.dp)
                    .testTag("edit_profile_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2. Compact Mode Card
// ---------------------------------------------------------------------------
@Composable
fun CompactModeCard(
    isCompact: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = ExpressiveCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isCompact) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCompact) Icons.Default.PhoneAndroid else Icons.Default.StayCurrentPortrait,
                            contentDescription = null,
                            tint = if (isCompact) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Compact Mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCompact) "Resized for small display phones" else "Optimized for standard screens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isCompact,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("compact_mode_switch")
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Dark & Light Mode Selector
// ---------------------------------------------------------------------------
@Composable
fun ThemeModeSelectorCard(
    currentMode: ThemeModeOption,
    onSelectMode: (ThemeModeOption) -> Unit
) {
    Surface(
        shape = ExpressiveCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Theme Appearance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Toggle between Light, Dark, or System mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeModeOption.values().forEach { mode ->
                    val isSelected = currentMode == mode
                    val icon = when (mode) {
                        ThemeModeOption.SYSTEM -> Icons.Default.BrightnessAuto
                        ThemeModeOption.LIGHT -> Icons.Default.LightMode
                        ThemeModeOption.DARK -> Icons.Default.DarkMode
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectMode(mode) }
                            .testTag("theme_mode_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. Material You Expressive Themes Card
// ---------------------------------------------------------------------------
@Composable
fun MaterialYouThemesCard(
    selectedScheme: AppThemeScheme,
    onSelectScheme: (AppThemeScheme) -> Unit
) {
    Surface(
        shape = ExpressiveCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Material You Expressive Themes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Curated dynamic color schemes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = selectedScheme.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AppThemeScheme.values()) { scheme ->
                    val isSelected = selectedScheme == scheme
                    val themeColor = when (scheme) {
                        AppThemeScheme.VIOLET -> MeyouViolet
                        AppThemeScheme.SAGE -> MeyouSagePrimary
                        AppThemeScheme.TERRACOTTA -> MeyouTerracottaPrimary
                        AppThemeScheme.OCEAN -> MeyouOceanPrimary
                        AppThemeScheme.ROSE -> MeyouRosePrimary
                        AppThemeScheme.MONOCHROME -> MeyouMonoPrimary
                        AppThemeScheme.DYNAMIC -> Color(0xFF5B5D72)
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .width(108.dp)
                            .clickable { onSelectScheme(scheme) }
                            .testTag("theme_chip_${scheme.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = themeColor,
                                modifier = Modifier.size(36.dp)
                            ) {
                                if (isSelected) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = scheme.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 5. Paper Texture Effects Card
// ---------------------------------------------------------------------------
@Composable
fun PaperTexturesCard(
    selectedTexture: PaperTexture,
    onSelectTexture: (PaperTexture) -> Unit
) {
    Surface(
        shape = ExpressiveCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Paper Texture Effects",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tactile paper surfaces for eBook and article reading",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PaperTexture.values()) { texture ->
                    val isSelected = selectedTexture == texture
                    val sampleBg = PageThemeCreamBg

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .width(100.dp)
                            .clickable { onSelectTexture(texture) }
                            .testTag("paper_texture_${texture.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .paperTexture(texture, sampleBg, isDark = false)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aa",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = PageThemeCreamText
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = texture.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = PageThemeCreamText
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 6. Reader Fonts Card
// ---------------------------------------------------------------------------
@Composable
fun ReaderFontsCard(
    selectedFont: ReadingFont,
    onSelectFont: (ReadingFont) -> Unit,
    onOpenMoreSettings: () -> Unit
) {
    Surface(
        shape = ExpressiveCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reader Fonts",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose typography style for reading",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = onOpenMoreSettings) {
                    Text("More settings", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ReadingFont.values()) { font ->
                    val isSelected = selectedFont == font
                    val fontFam = getReaderFontFamily(font)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clickable { onSelectFont(font) }
                            .testTag("profile_font_${font.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Ag",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = fontFam,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = font.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// StatMiniItem & ProfileMenuItem
// ---------------------------------------------------------------------------
@Composable
fun StatMiniItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

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

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Avatar View
// ---------------------------------------------------------------------------
@Composable
fun UserAvatarView(
    avatarUri: String?,
    presetId: String,
    size: Dp
) {
    val context = LocalContext.current
    val imageBitmap = remember(avatarUri) {
        val bmp = ImageHelper.loadSafeThumbnailBitmap(context, avatarUri, 250)
        bmp?.asImageBitmap()
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
    } else {
        val preset = AVATAR_PRESETS.find { it.id == presetId } ?: AVATAR_PRESETS.first()
        Surface(
            shape = CircleShape,
            color = preset.containerColor,
            border = BorderStroke(2.dp, preset.primaryColor),
            modifier = Modifier.size(size)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = preset.name,
                    tint = preset.primaryColor,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Edit Profile Dialog
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, nickname: String, age: Int, gender: String, avatarUri: String?, avatarPreset: String, bio: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf(currentProfile.name) }
    var nickname by remember { mutableStateOf(currentProfile.nickname) }
    var ageText by remember { mutableStateOf(currentProfile.age.toString()) }
    var selectedGender by remember { mutableStateOf(currentProfile.gender) }
    var avatarUri by remember { mutableStateOf(currentProfile.avatarUri) }
    var avatarPreset by remember { mutableStateOf(currentProfile.avatarPreset) }
    var bio by remember { mutableStateOf(currentProfile.bio) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = ImageHelper.saveCoverFromUri(context, uri, "avatar")
                avatarUri = savedPath ?: uri.toString()
            }
        }
    }

    val genderOptions = listOf("Female", "Male", "Non-binary", "Prefer not to say")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Customize Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Avatar / Photo Section
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatarView(
                            avatarUri = avatarUri,
                            presetId = avatarPreset,
                            size = 76.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = ExpressivePillShape,
                                modifier = Modifier.testTag("pick_photo_btn")
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pick Photo", style = MaterialTheme.typography.labelMedium)
                            }

                            if (avatarUri != null) {
                                OutlinedButton(
                                    onClick = { avatarUri = null },
                                    shape = ExpressivePillShape,
                                    modifier = Modifier.testTag("remove_photo_btn")
                                ) {
                                    Text("Remove", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Or choose an avatar character:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(AVATAR_PRESETS) { preset ->
                                val isSelected = avatarPreset == preset.id && avatarUri == null
                                Surface(
                                    shape = CircleShape,
                                    color = preset.containerColor,
                                    border = BorderStroke(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) preset.primaryColor else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable {
                                            avatarPreset = preset.id
                                            avatarUri = null
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = preset.icon,
                                            contentDescription = preset.name,
                                            tint = preset.primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Name field
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_name_input")
                    )
                }

                // Nickname field
                item {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { input ->
                            nickname = if (input.startsWith("@")) input else "@$input"
                        },
                        label = { Text("Nickname") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_nickname_input")
                    )
                }

                // Age field
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 3) {
                                    ageText = input
                                }
                            },
                            label = { Text("Age") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_age_input")
                        )

                        // Stepper buttons
                        IconButton(
                            onClick = {
                                val current = ageText.toIntOrNull() ?: 24
                                if (current > 5) ageText = (current - 1).toString()
                            }
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease Age")
                        }

                        IconButton(
                            onClick = {
                                val current = ageText.toIntOrNull() ?: 24
                                if (current < 120) ageText = (current + 1).toString()
                            }
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase Age")
                        }
                    }
                }

                // Gender Selection
                item {
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genderOptions.forEach { option ->
                            FilterChip(
                                selected = selectedGender == option,
                                onClick = { selectedGender = option },
                                label = { Text(option, style = MaterialTheme.typography.labelSmall) },
                                shape = ExpressivePillShape,
                                modifier = Modifier.testTag("gender_chip_${option.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // Bio field
                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Reading Bio / Motto") },
                        maxLines = 3,
                        leadingIcon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_bio_input")
                    )
                }

                // Save & Cancel Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val validAge = ageText.toIntOrNull() ?: 24
                                val validName = if (name.isBlank()) "Meyou Reader" else name
                                val validNickname = if (nickname.isBlank()) "@curator" else nickname
                                onSave(
                                    validName,
                                    validNickname,
                                    validAge,
                                    selectedGender,
                                    avatarUri,
                                    avatarPreset,
                                    bio
                                )
                            },
                            shape = ExpressivePillShape,
                            modifier = Modifier.testTag("save_profile_btn")
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}
