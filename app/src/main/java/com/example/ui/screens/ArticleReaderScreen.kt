package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.domain.model.Article
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.TextToSpeechManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    article: Article,
    preferences: ReadingPreferences,
    onBackClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddHighlight: (String, String) -> Unit, // text, note
    onUpdateArticleDetails: ((id: String, title: String, author: String, category: String, summary: String, colorHex: String, coverUri: String?) -> Unit)? = null,
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
    var highlightTextInput by remember { mutableStateOf("") }
    var highlightNoteInput by remember { mutableStateOf("") }
    var showShareSheet by remember { mutableStateOf(false) }
    var showCustomizeCoverDialog by remember { mutableStateOf(false) }

    // Determine colors from Reading Page Theme
    val (pageBgColor, pageTextColor) = when (preferences.pageTheme) {
        ReadingPageTheme.WARM -> Pair(PageThemeWarmBg, PageThemeWarmText)
        ReadingPageTheme.CREAM -> Pair(PageThemeCreamBg, PageThemeCreamText)
        ReadingPageTheme.LAVENDER -> Pair(PageThemeLavenderBg, PageThemeLavenderText)
        ReadingPageTheme.DARK -> Pair(PageThemeDarkBg, PageThemeDarkText)
    }

    val readerFontFamily = getReaderFontFamily(preferences.font)

    val paragraphs = remember(article.content) {
        if (article.content.isNotBlank()) {
            article.content.split(Regex("(\r?\n){2,}"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
        } else {
            listOf(
                "We are living through a profound inflection point in human labor. In every industry, machines and algorithmic models are rapidly mastering routine patterns.",
                "Yet far from rendering human insight obsolete, this transition highlights the irreplaceable nature of genuine curiosity, critical judgment, and cross-disciplinary empathy."
            )
        }
    }

    val contentMaxWidth = when (preferences.readingWidth) {
        ReadingWidth.COMPACT -> 560.dp
        ReadingWidth.STANDARD -> 680.dp
        ReadingWidth.WIDE -> 820.dp
    }

    val listState = rememberLazyListState()

    // Scroll state detection for hiding/showing floating toolbar
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

    // Auto-scroll when TTS moves to next paragraph
    LaunchedEffect(ttsManager.currentParagraphIndex, ttsManager.isPlaying) {
        if (ttsManager.isPlaying && paragraphs.isNotEmpty()) {
            val targetIndex = (ttsManager.currentParagraphIndex + 1).coerceIn(0, paragraphs.size)
            coroutineScope.launch {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("article_reader_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = article.source,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("reader_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = pageTextColor
                        )
                    }
                },
                actions = {
                    // TTS Top Button
                    IconButton(
                        onClick = {
                            if (ttsManager.isPlayerVisible) {
                                if (ttsManager.isPlaying) ttsManager.pause()
                                else ttsManager.resume()
                            } else {
                                ttsManager.startReading(paragraphs, 0)
                            }
                        },
                        modifier = Modifier.testTag("article_tts_top_btn")
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
                        modifier = Modifier.testTag("article_share_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share & Export",
                            tint = pageTextColor
                        )
                    }

                    // Customize Cover / Palette
                    IconButton(
                        onClick = { showCustomizeCoverDialog = true },
                        modifier = Modifier.testTag("article_customize_cover_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Customize Cover",
                            tint = pageTextColor
                        )
                    }

                    TactileBookmarkButton(
                        isBookmarked = article.isBookmarked,
                        onToggle = onBookmarkToggle
                    )

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("reader_settings_btn")
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
                // Article Metadata
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${article.category.uppercase()} · ${article.readTimeMinutes} MIN READ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = readerFontFamily,
                                fontSize = (preferences.fontSizeSp * 1.55f).sp,
                                lineHeight = (preferences.fontSizeSp * 1.9f).sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = pageTextColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "By ${article.author}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = pageTextColor.copy(alpha = 0.85f)
                            )
                            Text(
                                text = article.publishDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = pageTextColor.copy(alpha = 0.65f)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 20.dp),
                            color = pageTextColor.copy(alpha = 0.12f)
                        )
                    }
                }

                // Dynamic Article Body Paragraphs with TTS Auto-Highlight
                item {
                    SelectionContainer {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            val midIndex = (paragraphs.size / 2).coerceAtLeast(1)

                            paragraphs.forEachIndexed { index, paragraph ->
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

                                // Featured Quote in middle of article
                                if (index == midIndex - 1) {
                                    val quoteText = article.featuredQuote.ifBlank {
                                        if (paragraphs.size > 2) paragraphs[1].take(120) + "..." else "Curiosity is the beginning of every meaningful change."
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    AsymmetricQuoteCard(
                                        quote = quoteText,
                                        author = article.author,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Inbuilt TTS Player Bar (Docked at Bottom)
            TextToSpeechPlayerBar(
                ttsManager = ttsManager,
                totalParagraphs = paragraphs.size,
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

            // Floating Reading Toolbar (when TTS player is closed)
            if (!ttsManager.isPlayerVisible) {
                FloatingReadingToolbar(
                    isVisible = isToolbarVisible,
                    isBookmarked = article.isBookmarked,
                    onAaClick = onOpenSettings,
                    onBookmarkToggle = onBookmarkToggle,
                    onHighlightClick = {
                        highlightTextInput = article.featuredQuote.ifBlank { "Curiosity is the beginning of every meaningful change." }
                        showHighlightDialog = true
                    },
                    onShareClick = { showShareSheet = true },
                    onMoreClick = onOpenSettings,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }
    }

    // Share Format Bottom Sheet (PDF, TXT, CSV, JSON)
    if (showShareSheet) {
        ShareFormatBottomSheet(
            article = article,
            onDismiss = { showShareSheet = false }
        )
    }

    // Customize Cover / Album Art Dialog
    if (showCustomizeCoverDialog && onUpdateArticleDetails != null) {
        CustomizeBookCoverDialog(
            article = article,
            onSaveBook = { _, _, _, _, _, _, _ -> },
            onSaveArticle = { id, title, author, category, summary, colorHex, coverUri ->
                onUpdateArticleDetails(id, title, author, category, summary, colorHex, coverUri)
            },
            onDismiss = { showCustomizeCoverDialog = false }
        )
    }

    if (showHighlightDialog) {
        AlertDialog(
            onDismissRequest = { showHighlightDialog = false },
            title = { Text("Save Highlight") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "\"$highlightTextInput\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                    )
                    OutlinedTextField(
                        value = highlightNoteInput,
                        onValueChange = { highlightNoteInput = it },
                        label = { Text("Add personal note (optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("highlight_note_input")
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

