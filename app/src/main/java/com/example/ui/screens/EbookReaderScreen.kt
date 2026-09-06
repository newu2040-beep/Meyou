package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datastore.*
import com.example.domain.model.Book
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.TextToSpeechManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookReaderScreen(
    book: Book,
    preferences: ReadingPreferences,
    onBackClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddHighlight: (String, String) -> Unit,
    onUpdateProgress: (Int, Float) -> Unit,
    onUpdateBookDetails: ((id: String, title: String, author: String, category: String, synopsis: String, colorHex: String, coverUri: String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val ttsManager = remember { TextToSpeechManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.release()
        }
    }

    var isToolbarVisible by remember { mutableStateOf(true) }
    var showHighlightDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showCustomizeCoverDialog by remember { mutableStateOf(false) }
    var highlightTextInput by remember { mutableStateOf("") }
    var highlightNoteInput by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(book.currentPage) }

    val listState = rememberLazyListState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -12f) {
                    isToolbarVisible = false
                } else if (available.y > 12f) {
                    isToolbarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val (pageBgColor, pageTextColor) = when (preferences.pageTheme) {
        ReadingPageTheme.WARM -> Pair(PageThemeWarmBg, PageThemeWarmText)
        ReadingPageTheme.CREAM -> Pair(PageThemeCreamBg, PageThemeCreamText)
        ReadingPageTheme.LAVENDER -> Pair(PageThemeLavenderBg, PageThemeLavenderText)
        ReadingPageTheme.DARK -> Pair(PageThemeDarkBg, PageThemeDarkText)
    }

    val readerFontFamily = getReaderFontFamily(preferences.font)

    val contentMaxWidth = when (preferences.readingWidth) {
        ReadingWidth.COMPACT -> 560.dp
        ReadingWidth.STANDARD -> 680.dp
        ReadingWidth.WIDE -> 820.dp
    }

    val bookParagraphs = remember(book.content) {
        if (book.content.isNotBlank()) {
            book.content.split(Regex("(\r?\n){2,}"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    val paragraphsPerPage = 3
    val totalCalculatedPages = maxOf(1, if (bookParagraphs.isNotEmpty()) (bookParagraphs.size + paragraphsPerPage - 1) / paragraphsPerPage else book.totalPages)
    val pageIndex = (currentPage - 1).coerceIn(0, totalCalculatedPages - 1)
    val currentPageParagraphs = remember(bookParagraphs, pageIndex) {
        if (bookParagraphs.isEmpty()) {
            listOf("No content available for this book.")
        } else {
            val start = pageIndex * paragraphsPerPage
            val end = (start + paragraphsPerPage).coerceAtMost(bookParagraphs.size)
            if (start < bookParagraphs.size) {
                bookParagraphs.subList(start, end)
            } else {
                listOf(bookParagraphs.last())
            }
        }
    }

    // Auto-scroll effect when TTS progresses to next paragraph
    LaunchedEffect(ttsManager.currentParagraphIndex, ttsManager.isPlaying) {
        if (ttsManager.isPlaying && currentPageParagraphs.isNotEmpty()) {
            val targetIndex = (ttsManager.currentParagraphIndex + 1).coerceIn(0, currentPageParagraphs.size)
            coroutineScope.launch {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("ebook_reader_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = pageTextColor,
                            maxLines = 1
                        )
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = pageTextColor.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("ebook_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = pageTextColor
                        )
                    }
                },
                actions = {
                    // TTS Trigger in TopAppBar
                    IconButton(
                        onClick = {
                            if (ttsManager.isPlayerVisible) {
                                if (ttsManager.isPlaying) ttsManager.pause()
                                else ttsManager.resume()
                            } else {
                                ttsManager.startReading(currentPageParagraphs, 0)
                            }
                        },
                        modifier = Modifier.testTag("ebook_tts_top_btn")
                    ) {
                        Icon(
                            imageVector = if (ttsManager.isPlaying) Icons.Default.VolumeUp else Icons.Outlined.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = if (ttsManager.isPlaying) MaterialTheme.colorScheme.primary else pageTextColor
                        )
                    }

                    // Share Button (PDF, TXT, CSV, JSON)
                    IconButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier.testTag("ebook_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share & Export",
                            tint = pageTextColor
                        )
                    }

                    // Customize Cover / Album Art
                    IconButton(
                        onClick = { showCustomizeCoverDialog = true },
                        modifier = Modifier.testTag("ebook_customize_cover_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Customize Cover",
                            tint = pageTextColor
                        )
                    }

                    TactileBookmarkButton(
                        isBookmarked = book.isBookmarked,
                        onToggle = onBookmarkToggle
                    )

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("ebook_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = "Reading Settings",
                            tint = pageTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = pageBgColor,
                    titleContentColor = pageTextColor,
                    navigationIconContentColor = pageTextColor,
                    actionIconContentColor = pageTextColor
                )
            )
        },
        containerColor = pageBgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .paperTexture(
                    texture = preferences.paperTexture,
                    backgroundColor = pageBgColor,
                    isDark = preferences.pageTheme == ReadingPageTheme.DARK
                )
                .nestedScroll(nestedScrollConnection)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp)
            ) {
                // Chapter Header
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "CHAPTER 1",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = book.currentChapter,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = readerFontFamily
                            ),
                            fontWeight = FontWeight.Bold,
                            color = pageTextColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reading progress and page position
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Page $currentPage of ${book.totalPages}",
                                style = MaterialTheme.typography.labelSmall,
                                color = pageTextColor.copy(alpha = 0.65f)
                            )
                            Text(
                                text = "${((currentPage.toFloat() / totalCalculatedPages) * 100).toInt()}% completed",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        WavyProgressIndicator(
                            progress = (currentPage.toFloat() / totalCalculatedPages).coerceIn(0f, 1f),
                            waveColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 18.dp),
                            color = pageTextColor.copy(alpha = 0.12f)
                        )
                    }
                }

                // Dynamic Book Content for Current Page with TTS auto-highlighting
                item {
                    SelectionContainer {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            currentPageParagraphs.forEachIndexed { index, paragraph ->
                                val isCurrentlySpoken = (ttsManager.isPlaying || ttsManager.isPaused) &&
                                        ttsManager.currentParagraphIndex == index

                                val highlightBgColor by animateColorAsState(
                                    targetValue = if (isCurrentlySpoken) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    else Color.Transparent,
                                    label = "highlight_bg"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(highlightBgColor)
                                        .then(
                                            if (isCurrentlySpoken) Modifier.border(
                                                width = 1.5.dp,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) else Modifier
                                        )
                                        .padding(if (isCurrentlySpoken) 10.dp else 0.dp)
                                ) {
                                    Column {
                                        if (isCurrentlySpoken) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Reading Aloud",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Text(
                                            text = paragraph,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = readerFontFamily,
                                                fontSize = preferences.fontSizeSp.sp,
                                                lineHeight = (preferences.fontSizeSp * preferences.lineSpacingMultiplier).sp
                                            ),
                                            color = pageTextColor
                                        )
                                    }
                                }

                                // Show inspirational pullquote on first page if synopsis is available
                                if (currentPage == 1 && index == 0 && book.synopsis.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = "\"${book.synopsis}\"",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = readerFontFamily,
                                                fontStyle = FontStyle.Italic,
                                                lineHeight = 24.sp
                                            ),
                                            color = pageTextColor,
                                            modifier = Modifier.padding(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Next Page Navigation
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentPage > 1) {
                                    currentPage--
                                    onUpdateProgress(currentPage, currentPage.toFloat() / totalCalculatedPages)
                                    if (ttsManager.isPlaying) {
                                        ttsManager.startReading(currentPageParagraphs, 0)
                                    }
                                }
                            },
                            enabled = currentPage > 1,
                            shape = ExpressivePillShape
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }

                        Text(
                            text = "$currentPage / $totalCalculatedPages",
                            style = MaterialTheme.typography.bodySmall,
                            color = pageTextColor.copy(alpha = 0.6f)
                        )

                        Button(
                            onClick = {
                                if (currentPage < totalCalculatedPages) {
                                    currentPage++
                                    onUpdateProgress(currentPage, currentPage.toFloat() / totalCalculatedPages)
                                    if (ttsManager.isPlaying) {
                                        ttsManager.startReading(currentPageParagraphs, 0)
                                    }
                                }
                            },
                            enabled = currentPage < totalCalculatedPages,
                            shape = ExpressivePillShape
                        ) {
                            Text("Next Page")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Inbuilt TTS Player Bar (Docked at Bottom)
            TextToSpeechPlayerBar(
                ttsManager = ttsManager,
                totalParagraphs = currentPageParagraphs.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )

            // Floating Mini FAB to restore TTS player if minimized/temporarily hidden with X button
            FloatingTTSMiniFab(
                ttsManager = ttsManager,
                onClick = { ttsManager.showPlayer() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 80.dp)
                    .navigationBarsPadding()
            )

            // Floating Quick Toolbar if TTS player is not open
            if (isToolbarVisible && !ttsManager.isPlayerVisible) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 8.dp,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .navigationBarsPadding()
                        .testTag("ebook_floating_toolbar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // TTS Voice Quick Action
                        IconButton(
                            onClick = { ttsManager.startReading(currentPageParagraphs, 0) }
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = "Voice Reader")
                        }

                        // Share Quick Action
                        IconButton(onClick = { showShareSheet = true }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share")
                        }

                        // Font Settings Quick Action
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.TextFields, contentDescription = "Font settings")
                        }

                        TactileBookmarkButton(
                            isBookmarked = book.isBookmarked,
                            onToggle = onBookmarkToggle
                        )

                        IconButton(
                            onClick = {
                                highlightTextInput = "You do not rise to the level of your goals. You fall to the level of your systems."
                                showHighlightDialog = true
                            }
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Highlight")
                        }
                    }
                }
            }
        }
    }

    // Share & Export Bottom Sheet (PDF, TXT, CSV, JSON)
    if (showShareSheet) {
        ShareFormatBottomSheet(
            book = book,
            onDismiss = { showShareSheet = false }
        )
    }

    // Customize Album Art / Cover Dialog
    if (showCustomizeCoverDialog && onUpdateBookDetails != null) {
        CustomizeBookCoverDialog(
            book = book,
            onSaveBook = { id, title, author, category, synopsis, colorHex, coverUri ->
                onUpdateBookDetails(id, title, author, category, synopsis, colorHex, coverUri)
            },
            onSaveArticle = { _, _, _, _, _, _, _ -> },
            onDismiss = { showCustomizeCoverDialog = false }
        )
    }

    // Highlight Note Dialog
    if (showHighlightDialog) {
        AlertDialog(
            onDismissRequest = { showHighlightDialog = false },
            title = { Text("Highlight Selected Text") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "\"$highlightTextInput\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                    )
                    OutlinedTextField(
                        value = highlightNoteInput,
                        onValueChange = { highlightNoteInput = it },
                        label = { Text("Note (optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("ebook_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddHighlight(highlightTextInput, highlightNoteInput)
                        showHighlightDialog = false
                        highlightNoteInput = ""
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHighlightDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

