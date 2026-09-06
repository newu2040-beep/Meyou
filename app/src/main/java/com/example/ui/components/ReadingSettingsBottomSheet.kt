package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datastore.ReadingFont
import com.example.data.datastore.ReadingPageTheme
import com.example.data.datastore.ReadingPreferences
import com.example.data.datastore.ReadingWidth
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSettingsBottomSheet(
    preferences: ReadingPreferences,
    onFontSizeChange: (Float) -> Unit,
    onFontChange: (ReadingFont) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onPageThemeChange: (ReadingPageTheme) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onReadingWidthChange: (ReadingWidth) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag("reading_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Reading settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Text Size Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Text size",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${preferences.fontSizeSp.toInt()} pt",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = preferences.fontSizeSp,
                onValueChange = onFontSizeChange,
                valueRange = 14f..28f,
                steps = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("font_size_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Font Family
            Text(
                text = "Font",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReadingFont.values().forEach { font ->
                    val isSelected = preferences.font == font
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFontChange(font) },
                        label = {
                            Text(
                                text = font.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = ExpressivePillShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("font_chip_${font.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Page Color Theme
            Text(
                text = "Page color",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReadingPageTheme.values().forEach { theme ->
                    val isSelected = preferences.pageTheme == theme
                    val bgColor = when (theme) {
                        ReadingPageTheme.WARM -> PageThemeWarmBg
                        ReadingPageTheme.CREAM -> PageThemeCreamBg
                        ReadingPageTheme.LAVENDER -> PageThemeLavenderBg
                        ReadingPageTheme.DARK -> PageThemeDarkBg
                    }
                    val textColor = when (theme) {
                        ReadingPageTheme.WARM -> PageThemeWarmText
                        ReadingPageTheme.CREAM -> PageThemeCreamText
                        ReadingPageTheme.LAVENDER -> PageThemeLavenderText
                        ReadingPageTheme.DARK -> PageThemeDarkText
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onPageThemeChange(theme) }
                            .testTag("page_theme_${theme.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = theme.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Line Spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Line spacing",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = String.format("%.1fx", preferences.lineSpacingMultiplier),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = preferences.lineSpacingMultiplier,
                onValueChange = onLineSpacingChange,
                valueRange = 1.2f..2.2f,
                steps = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("line_spacing_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Brightness
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BrightnessMedium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Brightness",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${(preferences.brightness * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = preferences.brightness,
                onValueChange = onBrightnessChange,
                valueRange = 0.2f..1.0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("brightness_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reading Width
            Text(
                text = "Reading width",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReadingWidth.values().forEach { width ->
                    val isSelected = preferences.readingWidth == width
                    FilterChip(
                        selected = isSelected,
                        onClick = { onReadingWidthChange(width) },
                        label = {
                            Text(
                                text = width.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = ExpressivePillShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("width_chip_${width.name.lowercase()}")
                    )
                }
            }
        }
    }
}
