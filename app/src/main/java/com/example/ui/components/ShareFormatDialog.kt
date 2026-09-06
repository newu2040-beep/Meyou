package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.domain.model.Article
import com.example.domain.model.Book
import com.example.util.ExportFormat
import com.example.util.ShareExportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareFormatBottomSheet(
    book: Book? = null,
    article: Article? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val title = book?.title ?: article?.title ?: "Document"
    val author = book?.author ?: article?.author ?: ""

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
                .padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Share & Export",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$title · $author",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SELECT EXPORT FORMAT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val formatOptions = listOf(
                FormatCardData(
                    format = ExportFormat.PDF,
                    title = "PDF Document",
                    subtitle = "Styled A4 pages with typography, margins & progress",
                    icon = Icons.Outlined.PictureAsPdf,
                    badgeColor = Color(0xFFE53935)
                ),
                FormatCardData(
                    format = ExportFormat.TXT,
                    title = "Plain Text (.txt)",
                    subtitle = "Universal UTF-8 formatted plain text for any device",
                    icon = Icons.Outlined.Description,
                    badgeColor = Color(0xFF1E88E5)
                ),
                FormatCardData(
                    format = ExportFormat.CSV,
                    title = "Spreadsheet (.csv)",
                    subtitle = "Tabular data with columns for metadata & content",
                    icon = Icons.Outlined.TableChart,
                    badgeColor = Color(0xFF43A047)
                ),
                FormatCardData(
                    format = ExportFormat.JSON,
                    title = "Structured Data (.json)",
                    subtitle = "Full JSON payload with progress, quotes & schema",
                    icon = Icons.Outlined.Code,
                    badgeColor = Color(0xFFFB8C00)
                )
            )

            formatOptions.forEach { option ->
                FormatOptionCard(
                    data = option,
                    onClick = {
                        if (book != null) {
                            ShareExportHelper.exportAndShareBook(context, book, option.format)
                        } else if (article != null) {
                            ShareExportHelper.exportAndShareArticle(context, article, option.format)
                        }
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

private data class FormatCardData(
    val format: ExportFormat,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeColor: Color
)

@Composable
private fun FormatOptionCard(
    data: FormatCardData,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("share_format_${data.format.extension}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(data.badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
