package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.TextToSpeechManager
import com.example.util.VoicePreset

@Composable
fun TextToSpeechPlayerBar(
    ttsManager: TextToSpeechManager,
    totalParagraphs: Int,
    modifier: Modifier = Modifier
) {
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = ttsManager.isPlayerVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("tts_player_bar")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top row: Spoken preview snippet + Timer badge + Close (cross) button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (ttsManager.isPlaying) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (ttsManager.isPlaying) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (ttsManager.isPlaying) "Reading Paragraph ${ttsManager.currentParagraphIndex + 1} of $totalParagraphs"
                            else if (ttsManager.isPaused) "Paused at Paragraph ${ttsManager.currentParagraphIndex + 1}"
                            else "Voice Reader Ready",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (ttsManager.currentSpokenText.isNotBlank()) ttsManager.currentSpokenText
                            else "Tap play to start listening with voice assistant",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Sleep Timer Badge if active
                    if (ttsManager.sleepTimerRemainingSeconds > 0) {
                        val minutes = ttsManager.sleepTimerRemainingSeconds / 60
                        val seconds = ttsManager.sleepTimerRemainingSeconds % 60
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clickable { showTimerDialog = true }
                                .padding(end = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%02d:%02d", minutes, seconds),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Temporary Hide / Cross Button
                    IconButton(
                        onClick = { ttsManager.hidePlayer() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("tts_hide_cross_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hide Player Temporarily",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Middle Row: Primary Playback Controls
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Voice Preset Button
                    AssistChip(
                        onClick = { showVoiceDialog = true },
                        label = { Text(ttsManager.selectedVoicePreset.label, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("tts_voice_chip")
                    )

                    // Previous Paragraph Button
                    IconButton(
                        onClick = { ttsManager.previousParagraph() },
                        modifier = Modifier.testTag("tts_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Paragraph",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Main Play / Pause Button
                    FilledIconButton(
                        onClick = {
                            if (ttsManager.isPlaying) {
                                ttsManager.pause()
                            } else if (ttsManager.isPaused) {
                                ttsManager.resume()
                            } else {
                                ttsManager.resume()
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("tts_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (ttsManager.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (ttsManager.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Next Paragraph Button
                    IconButton(
                        onClick = { ttsManager.nextParagraph() },
                        modifier = Modifier.testTag("tts_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Paragraph",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Speed Selector Button
                    AssistChip(
                        onClick = { showSpeedDialog = true },
                        label = { Text("${ttsManager.playbackSpeed}x", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("tts_speed_chip")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Quick Action Badges: Stop & Sleep Timer
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = { ttsManager.stop() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop", fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = { showTimerDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (ttsManager.sleepTimerRemainingSeconds > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tts_sleep_timer_button")
                    ) {
                        Icon(Icons.Outlined.Bedtime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (ttsManager.sleepTimerRemainingSeconds > 0) "Timer Active" else "Sleep Timer",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Voice Selection Dialog
    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null) },
            title = { Text("Select Voice & Pitch") },
            text = {
                Column {
                    Text(
                        text = "Choose an expressive speaking personality:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    VoicePreset.values().forEach { preset ->
                        Surface(
                            onClick = {
                                ttsManager.applyVoicePreset(preset)
                                showVoiceDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (ttsManager.selectedVoicePreset == preset) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                RadioButton(
                                    selected = ttsManager.selectedVoicePreset == preset,
                                    onClick = {
                                        ttsManager.applyVoicePreset(preset)
                                        showVoiceDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = preset.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Speed: ${preset.speechRate}x · Pitch: ${preset.pitch}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Speed Selection Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            icon = { Icon(Icons.Default.Speed, contentDescription = null) },
            title = { Text("Playback Speed") },
            text = {
                Column {
                    speeds.forEach { spd ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ttsManager.setSpeed(spd)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = (ttsManager.playbackSpeed == spd),
                                onClick = {
                                    ttsManager.setSpeed(spd)
                                    showSpeedDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${spd}x ${if (spd == 1.0f) "(Normal)" else ""}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Sleep Timer Dialog
    if (showTimerDialog) {
        val timerOptions = listOf(5, 10, 15, 30, 45, 60)
        var customMinutesInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            icon = { Icon(Icons.Default.Bedtime, contentDescription = null) },
            title = { Text("Background Sleep Timer") },
            text = {
                Column {
                    Text(
                        text = "Automatically stops speech playback after selected duration for nighttime reading:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(timerOptions) { min ->
                            FilterChip(
                                selected = ttsManager.sleepTimerMinutes == min,
                                onClick = {
                                    ttsManager.setSleepTimer(min)
                                    showTimerDialog = false
                                },
                                label = { Text("$min min") },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = customMinutesInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) customMinutesInput = it },
                        label = { Text("Custom Minutes") },
                        singleLine = true,
                        trailingIcon = {
                            if (customMinutesInput.isNotBlank()) {
                                IconButton(onClick = {
                                    val mins = customMinutesInput.toIntOrNull()
                                    if (mins != null && mins > 0) {
                                        ttsManager.setSleepTimer(mins)
                                        showTimerDialog = false
                                    }
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Apply")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (ttsManager.sleepTimerRemainingSeconds > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                ttsManager.cancelSleepTimer()
                                showTimerDialog = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.TimerOff, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Turn Off Timer")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun FloatingTTSMiniFab(
    ttsManager: TextToSpeechManager,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!ttsManager.isPlayerVisible && (ttsManager.isPlaying || ttsManager.isPaused)) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = modifier
                .size(48.dp)
                .testTag("tts_mini_fab")
        ) {
            Icon(
                imageVector = if (ttsManager.isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                contentDescription = "Show TTS Player",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
