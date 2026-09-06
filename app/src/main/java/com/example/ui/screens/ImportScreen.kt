package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressivePillShape
import com.example.util.ContentImporter
import com.example.util.ImportType
import com.example.util.ParsedImport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBackClick: () -> Unit,
    onImportSuccess: (ParsedImport) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Device File", "Paste Text", "Sample Presets")

    // State for file picker
    var pendingFileImport by remember { mutableStateOf<ParsedImport?>(null) }
    var fileParsingError by remember { mutableStateOf<String?>(null) }

    // Launcher for selecting documents
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                fileParsingError = null
                val parsed = ContentImporter.parseFromUri(context, uri)
                pendingFileImport = parsed
            } catch (e: Exception) {
                fileParsingError = "Failed to parse file: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    // State for manual paste
    var manualTitle by remember { mutableStateOf("") }
    var manualAuthor by remember { mutableStateOf("") }
    var manualCategory by remember { mutableStateOf("eBooks") }
    var manualContent by remember { mutableStateOf("") }
    var manualType by remember { mutableStateOf(ImportType.EBOOK) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("import_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Import Reading Material",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "EPUB, Markdown, Text, Articles & eBooks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("import_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("import_tab_$index")
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // FILE IMPORT TAB
                    FileImportTab(
                        pendingImport = pendingFileImport,
                        errorMessage = fileParsingError,
                        onPickFileClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "text/*",
                                    "application/epub+zip",
                                    "application/pdf",
                                    "application/json",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        },
                        onUpdateImport = { updated -> pendingFileImport = updated },
                        onConfirmImport = { importItem ->
                            onImportSuccess(importItem)
                        },
                        onClearPending = { pendingFileImport = null }
                    )
                }

                1 -> {
                    // PASTE TEXT TAB
                    PasteTextTab(
                        title = manualTitle,
                        onTitleChange = { manualTitle = it },
                        author = manualAuthor,
                        onAuthorChange = { manualAuthor = it },
                        category = manualCategory,
                        onCategoryChange = { manualCategory = it },
                        content = manualContent,
                        onContentChange = { manualContent = it },
                        type = manualType,
                        onTypeChange = { manualType = it },
                        onPasteFromClipboard = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                            if (!clipText.isNullOrBlank()) {
                                manualContent = clipText
                                if (manualTitle.isBlank()) {
                                    val firstLine = clipText.lines().firstOrNull { it.isNotBlank() } ?: ""
                                    manualTitle = firstLine.take(50).removePrefix("#").trim()
                                }
                            }
                        },
                        onImportClick = {
                            if (manualContent.isNotBlank()) {
                                val item = ContentImporter.createParsedImport(
                                    title = manualTitle.ifBlank { "Pasted Reading" },
                                    author = manualAuthor.ifBlank { "Author" },
                                    category = manualCategory,
                                    content = manualContent,
                                    forceType = manualType
                                )
                                onImportSuccess(item)
                            }
                        }
                    )
                }

                2 -> {
                    // PRESET SAMPLES TAB
                    PresetSamplesTab(
                        presets = ContentImporter.samplePresets,
                        onSelectPreset = { preset ->
                            onImportSuccess(preset)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FileImportTab(
    pendingImport: ParsedImport?,
    errorMessage: String?,
    onPickFileClick: () -> Unit,
    onUpdateImport: (ParsedImport) -> Unit,
    onConfirmImport: (ParsedImport) -> Unit,
    onClearPending: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = ExpressiveCardShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        if (pendingImport == null) {
            // Upload Prompt Card
            item {
                Surface(
                    shape = ExpressiveCardShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = ExpressiveCardShape
                        )
                        .clickable { onPickFileClick() }
                        .testTag("file_picker_dropzone")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.UploadFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Tap to choose a file",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Supports EPUB eBooks, Markdown (.md), Plain Text (.txt), HTML & JSON documents",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onPickFileClick,
                            shape = ExpressivePillShape,
                            modifier = Modifier.testTag("select_file_button")
                        ) {
                            Icon(imageVector = Icons.Default.FileOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse Device Files")
                        }
                    }
                }
            }

            item {
                Card(
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "What happens when you import?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• MEYOU extracts chapters and paragraphs seamlessly\n• Words and estimated reading time are automatically calculated\n• Stored securely on your device with offline access\n• Highlights, notes, and reading progress sync locally",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Preview & Edit Parsed File
            item {
                Card(
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "File Parsed Successfully",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = onClearPending,
                                modifier = Modifier.testTag("clear_file_btn")
                            ) {
                                Text("Choose Another")
                            }
                        }

                        if (pendingImport.originalFileName != null) {
                            Text(
                                text = "Source: ${pendingImport.originalFileName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = pendingImport.title,
                            onValueChange = { onUpdateImport(pendingImport.copy(title = it)) },
                            label = { Text("Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_file_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pendingImport.author,
                            onValueChange = { onUpdateImport(pendingImport.copy(author = it)) },
                            label = { Text("Author") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_file_author_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Reading Format:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = pendingImport.importType == ImportType.EBOOK,
                                onClick = { onUpdateImport(pendingImport.copy(importType = ImportType.EBOOK)) },
                                label = { Text("eBook (Paging & Chapters)") },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                shape = ExpressivePillShape,
                                modifier = Modifier.testTag("type_ebook_chip")
                            )

                            FilterChip(
                                selected = pendingImport.importType == ImportType.ARTICLE,
                                onClick = { onUpdateImport(pendingImport.copy(importType = ImportType.ARTICLE)) },
                                label = { Text("Article (Flow Scroll)") },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Outlined.Article, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                shape = ExpressivePillShape,
                                modifier = Modifier.testTag("type_article_chip")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Word count & info
                        val wordCount = pendingImport.content.split(Regex("\\s+")).count { it.isNotBlank() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$wordCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Words",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (pendingImport.importType == ImportType.EBOOK) "${pendingImport.estimatedPagesOrMinutes} pages" else "${pendingImport.estimatedPagesOrMinutes} min",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (pendingImport.importType == ImportType.EBOOK) "Est. Pages" else "Read Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Content Preview:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = pendingImport.content.take(300) + if (pendingImport.content.length > 300) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { onConfirmImport(pendingImport) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_import_button"),
                            shape = ExpressivePillShape
                        ) {
                            Text("Import & Start Reading", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasteTextTab(
    title: String,
    onTitleChange: (String) -> Unit,
    author: String,
    onAuthorChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    type: ImportType,
    onTypeChange: (ImportType) -> Unit,
    onPasteFromClipboard: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("eBooks", "Articles", "Mindset", "Technology", "Science", "Philosophy")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Direct Content Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = onPasteFromClipboard,
                    shape = ExpressivePillShape,
                    modifier = Modifier.testTag("paste_clipboard_btn")
                ) {
                    Icon(imageVector = Icons.Outlined.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Paste Clipboard", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                placeholder = { Text("e.g., The Principles of Deep Work") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_title_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = author,
                onValueChange = onAuthorChange,
                label = { Text("Author") },
                placeholder = { Text("e.g., Cal Newport") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_author_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            Text(
                text = "Format & Category:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = type == ImportType.EBOOK,
                    onClick = { onTypeChange(ImportType.EBOOK) },
                    label = { Text("eBook") },
                    shape = ExpressivePillShape,
                    modifier = Modifier.testTag("paste_type_ebook")
                )

                FilterChip(
                    selected = type == ImportType.ARTICLE,
                    onClick = { onTypeChange(ImportType.ARTICLE) },
                    label = { Text("Article") },
                    shape = ExpressivePillShape,
                    modifier = Modifier.testTag("paste_type_article")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.take(4).forEach { cat ->
                    val isSelected = category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                        shape = ExpressivePillShape
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text("Body Text / Content") },
                placeholder = { Text("Paste book chapters, article text, or notes here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp)
                    .testTag("manual_content_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Button(
                onClick = onImportClick,
                enabled = content.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("manual_import_btn"),
                shape = ExpressivePillShape
            ) {
                Text("Import & Read Now", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PresetSamplesTab(
    presets: List<ParsedImport>,
    onSelectPreset: (ParsedImport) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Instant 1-Tap Classic Imports",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enjoy public domain classics and in-depth essays immediately without needing a local file.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(presets) { preset ->
            Card(
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPreset(preset) }
                    .testTag("preset_card_${preset.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cover badge
                    val coverBg = try {
                        Color(android.graphics.Color.parseColor(preset.accentColorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = coverBg,
                        modifier = Modifier
                            .width(52.dp)
                            .height(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (preset.importType == ImportType.EBOOK) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.Article,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = ExpressivePillShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = preset.importType.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = if (preset.importType == ImportType.EBOOK) "${preset.estimatedPagesOrMinutes} pages" else "${preset.estimatedPagesOrMinutes} min read",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = preset.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = preset.summaryOrSynopsis,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    FilledTonalIconButton(
                        onClick = { onSelectPreset(preset) },
                        modifier = Modifier.testTag("import_preset_btn_${preset.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Import and read"
                        )
                    }
                }
            }
        }
    }
}
