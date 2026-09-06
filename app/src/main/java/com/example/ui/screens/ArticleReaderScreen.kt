package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datastore.*
import com.example.domain.model.Article
import com.example.ui.components.AsymmetricQuoteCard
import com.example.ui.components.FloatingReadingToolbar
import com.example.ui.components.TactileBookmarkButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    article: Article,
    preferences: ReadingPreferences,
    onBackClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddHighlight: (String, String) -> Unit, // text, note
    modifier: Modifier = Modifier
) {
    var isToolbarVisible by remember { mutableStateOf(true) }
    var showHighlightDialog by remember { mutableStateOf(false) }
    var highlightTextInput by remember { mutableStateOf("") }
    var highlightNoteInput by remember { mutableStateOf("") }
    var showShareDialog by remember { mutableStateOf(false) }

    // Scroll state detection for hiding/showing floating toolbar
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -12f) {
                    // Scrolling down
                    isToolbarVisible = false
                } else if (available.y > 12f) {
                    // Scrolling up
                    isToolbarVisible = true
                }
                return Offset.Zero
            }
        }
    }

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
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
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
                            contentDescription = "Reading Settings"
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
                .nestedScroll(nestedScrollConnection)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
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

                // Dynamic Article Body Paragraphs
                item {
                    SelectionContainer {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            val midIndex = (paragraphs.size / 2).coerceAtLeast(1)
                            val firstHalf = paragraphs.take(midIndex)
                            val secondHalf = paragraphs.drop(midIndex)

                            firstHalf.forEach { paragraph ->
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

                            val quoteText = article.featuredQuote.ifBlank {
                                if (paragraphs.size > 2) paragraphs[1].take(120) + "..." else "Curiosity is the beginning of every meaningful change."
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            AsymmetricQuoteCard(
                                quote = quoteText,
                                author = article.author,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            secondHalf.forEach { paragraph ->
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
                    }
                }
            }

            // Floating Reading Toolbar (Material 3 Expressive)
            FloatingReadingToolbar(
                isVisible = isToolbarVisible,
                isBookmarked = article.isBookmarked,
                onAaClick = onOpenSettings,
                onBookmarkToggle = onBookmarkToggle,
                onHighlightClick = {
                    highlightTextInput = "Curiosity is the beginning of every meaningful change."
                    showHighlightDialog = true
                },
                onShareClick = { showShareDialog = true },
                onMoreClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
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

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Article") },
            text = {
                Text("Share \"${article.title}\" by ${article.author} via link or quote image.")
            },
            confirmButton = {
                Button(onClick = { showShareDialog = false }) {
                    Text("Copy Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
