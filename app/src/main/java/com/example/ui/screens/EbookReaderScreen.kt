package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.domain.model.Book
import com.example.ui.components.TactileBookmarkButton
import com.example.ui.components.WavyProgressIndicator
import com.example.ui.theme.*

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
    modifier: Modifier = Modifier
) {
    var isToolbarVisible by remember { mutableStateOf(true) }
    var showHighlightDialog by remember { mutableStateOf(false) }
    var highlightTextInput by remember { mutableStateOf("") }
    var highlightNoteInput by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(book.currentPage) }

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
                            color = pageTextColor
                        )
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = pageTextColor.copy(alpha = 0.7f)
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
                .nestedScroll(nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
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

                // Dynamic Book Content for Current Page with selection support
                item {
                    SelectionContainer {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            currentPageParagraphs.forEachIndexed { index, paragraph ->
                                Text(
                                    text = paragraph,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = readerFontFamily,
                                        fontSize = preferences.fontSizeSp.sp,
                                        lineHeight = (preferences.fontSizeSp * preferences.lineSpacingMultiplier).sp
                                    ),
                                    color = pageTextColor
                                )

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

            // Floating Toolbar
            if (isToolbarVisible) {
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
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.TextFields, contentDescription = "Font settings")
                        }

                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.BrightnessMedium, contentDescription = "Brightness")
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

                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                        }
                    }
                }
            }
        }
    }

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
