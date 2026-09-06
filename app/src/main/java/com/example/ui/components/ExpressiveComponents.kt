package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Book
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Tactile Bookmark button with spring-based scale, overshoot, and morph animation.
 */
@Composable
fun TactileBookmarkButton(
    isBookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val rotationAnim = remember { Animatable(0f) }

    IconButton(
        onClick = {
            coroutineScope.launch {
                // Tactile feedback: compress -> scale overshoot -> settle
                scaleAnim.animateTo(
                    targetValue = 0.72f,
                    animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
                )
                scaleAnim.animateTo(
                    targetValue = 1.35f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                rotationAnim.animateTo(
                    targetValue = if (!isBookmarked) 15f else -15f,
                    animationSpec = tween(durationMillis = 90)
                )
                rotationAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
                )
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
                )
            }
            onToggle()
        },
        modifier = modifier
            .testTag("bookmark_toggle_btn")
            .size(48.dp)
            .scale(scaleAnim.value)
    ) {
        if (isBookmarked) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = "Remove bookmark",
                tint = activeColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = "Save bookmark",
                tint = inactiveColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Material 3 Expressive Wavy Progress Indicator.
 */
@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
    waveHeight: Float = 6f,
    wavelength: Float = 40f
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val infiniteTransition = rememberInfiniteTransition(label = "wave_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .testTag("wavy_progress_bar")
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val activeWidth = width * clampedProgress

        // Background track (subtle straight line)
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 4.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Active wavy line
        if (activeWidth > 2f) {
            val path = Path()
            path.moveTo(0f, centerY)

            var x = 0f
            val step = 3f
            while (x <= activeWidth) {
                val y = centerY + sin((x / wavelength) * (2 * PI).toFloat() + phase) * waveHeight
                path.lineTo(x, y)
                x += step
            }

            drawPath(
                path = path,
                color = waveColor,
                style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

/**
 * Featured continue reading card with expressive container geometry.
 */
@Composable
fun FeaturedContinueReadingCard(
    book: Book,
    onContinueClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = ExpressiveHeroCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("featured_continue_reading_card")
            .clickable { onContinueClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "CURRENTLY READING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                TactileBookmarkButton(
                    isBookmarked = book.isBookmarked,
                    onToggle = onBookmarkToggle
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book visual cover thumbnail
                BookArticleCoverThumbnail(
                    customCoverUri = book.customCoverUri,
                    accentColorHex = book.accentColorHex,
                    title = book.title,
                    isArticle = false,
                    cornerRadius = 10,
                    modifier = Modifier.size(width = 72.dp, height = 104.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = book.currentChapter,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Page ${book.currentPage} of ${book.totalPages}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(book.progressPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wavy progress indicator
            WavyProgressIndicator(
                progress = book.progressPercent,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            FilledTonalButton(
                onClick = onContinueClick,
                shape = ExpressivePillShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continue_reading_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continue Reading",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Mid-article asymmetrical quote container with organic expressive geometry.
 */
@Composable
fun AsymmetricQuoteCard(
    quote: String,
    author: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = ExpressiveQuoteShape,
        color = MeyouLavenderContainer,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("asymmetric_quote_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.FormatQuote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = quote,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontStyle = FontStyle.Italic,
                    lineHeight = 28.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )

            if (!author.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "— $author",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Floating Reading Toolbar that hides on scroll down and restores on scroll up.
 */
@Composable
fun FloatingReadingToolbar(
    isVisible: Boolean,
    isBookmarked: Boolean,
    onAaClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onHighlightClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it * 2 }),
        exit = slideOutVertically(targetOffsetY = { it * 2 }),
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp,
            tonalElevation = 6.dp,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .testTag("floating_reading_toolbar")
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onAaClick,
                    modifier = Modifier.testTag("toolbar_aa_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "Text Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TactileBookmarkButton(
                    isBookmarked = isBookmarked,
                    onToggle = onBookmarkToggle,
                    activeColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onHighlightClick,
                    modifier = Modifier.testTag("toolbar_highlight_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Highlight",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("toolbar_share_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.testTag("toolbar_more_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
