package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.datastore.*
import com.example.data.local.database.MeyouDatabase
import com.example.data.repository.MeyouRepository
import com.example.ui.components.ReadingSettingsBottomSheet
import com.example.ui.screens.*
import com.example.ui.theme.MEYOUTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    private val viewModel: MeyouViewModel by viewModels {
        val database = MeyouDatabase.getDatabase(applicationContext)
        val repository = MeyouRepository(database)
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        MeyouViewModelFactory(repository, preferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isDarkTheme = when (uiState.preferences.themeMode) {
                com.example.data.datastore.ThemeModeOption.DARK -> true
                com.example.data.datastore.ThemeModeOption.LIGHT -> false
                com.example.data.datastore.ThemeModeOption.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MEYOUTheme(
                themeScheme = uiState.preferences.themeScheme,
                darkTheme = isDarkTheme,
                isCompactMode = uiState.preferences.isCompactMode
            ) {
                MeyouApp(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}

@Composable
fun MeyouApp(viewModel: MeyouViewModel, uiState: MeyouUiState) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar messages
    LaunchedEffect(uiState.userSnackbarMessage) {
        uiState.userSnackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbar()
        }
    }

    val isTopLevelScreen = when (uiState.currentScreen) {
        Screen.Home, Screen.Explore, Screen.Library, Screen.Bookmarks, Screen.Profile -> true
        else -> false
    }

    // Handle back press
    BackHandler(enabled = !isTopLevelScreen || uiState.currentScreen !is Screen.Home) {
        when (uiState.currentScreen) {
            is Screen.ArticleReader, is Screen.EbookReader, is Screen.Search, is Screen.ReadingStatistics, is Screen.Import -> {
                viewModel.navigateTo(Screen.Home)
            }
            is Screen.Explore, is Screen.Library, is Screen.Bookmarks, is Screen.Profile -> {
                viewModel.navigateTo(Screen.Home)
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isTopLevelScreen) {
                MeyouBottomNavBar(
                    currentScreen = uiState.currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (isTopLevelScreen) innerPadding.calculateBottomPadding() else 0.dp
                )
        ) {
            AnimatedContent(
                targetState = uiState.currentScreen,
                label = "screen_transition",
                transitionSpec = {
                    if (targetState is Screen.ArticleReader || targetState is Screen.EbookReader) {
                        (fadeIn(animationSpec = androidx.compose.animation.core.tween(260)) +
                                scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)
                                ))
                            .togetherWith(
                                fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                                        scaleOut(targetScale = 0.96f)
                            )
                    } else if (initialState is Screen.ArticleReader || initialState is Screen.EbookReader) {
                        (fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                                scaleIn(initialScale = 1.04f))
                            .togetherWith(
                                fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                                        scaleOut(targetScale = 0.94f)
                            )
                    } else {
                        (fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                                slideInHorizontally(
                                    initialOffsetX = { width -> (width * 0.08f).toInt() },
                                    animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)
                                )).togetherWith(
                            fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                                    slideOutHorizontally(targetOffsetX = { width -> (-width * 0.08f).toInt() })
                        )
                    }
                }
            ) { targetScreen ->
                when (targetScreen) {
                    is Screen.Onboarding -> {
                        OnboardingScreen(
                            onGetStarted = { viewModel.setOnboardingComplete() }
                        )
                    }

                    is Screen.Home -> {
                        HomeScreen(
                            books = uiState.books,
                            articles = uiState.articles,
                            selectedCategory = uiState.selectedHomeCategory,
                            onSelectCategory = { viewModel.setHomeCategory(it) },
                            onBookClick = { bookId -> viewModel.navigateTo(Screen.EbookReader(bookId)) },
                            onArticleClick = { articleId -> viewModel.navigateTo(Screen.ArticleReader(articleId)) },
                            onBookmarkBook = { book ->
                                viewModel.toggleBookmark(
                                    itemId = book.id,
                                    itemType = "BOOK",
                                    title = book.title,
                                    subtitle = book.author,
                                    category = "eBook"
                                )
                            },
                            onBookmarkArticle = { article ->
                                viewModel.toggleBookmark(
                                    itemId = article.id,
                                    itemType = "ARTICLE",
                                    title = article.title,
                                    subtitle = article.author,
                                    category = article.category
                                )
                            },
                            onSearchClick = { viewModel.navigateTo(Screen.Search) },
                            onProfileClick = { viewModel.navigateTo(Screen.Profile) },
                            onImportClick = { viewModel.navigateTo(Screen.Import) }
                        )
                    }

                    is Screen.Explore -> {
                        ExploreScreen(
                            articles = uiState.articles,
                            selectedTopic = uiState.selectedExploreTopic,
                            onSelectTopic = { viewModel.setExploreTopic(it) },
                            onArticleClick = { articleId -> viewModel.navigateTo(Screen.ArticleReader(articleId)) },
                            onBookmarkToggle = { article ->
                                viewModel.toggleBookmark(
                                    itemId = article.id,
                                    itemType = "ARTICLE",
                                    title = article.title,
                                    subtitle = article.author,
                                    category = article.category
                                )
                            },
                            onSearchClick = { viewModel.navigateTo(Screen.Search) }
                        )
                    }

                    is Screen.Library -> {
                        LibraryScreen(
                            books = uiState.books,
                            articles = uiState.articles,
                            selectedTab = uiState.selectedLibraryTab,
                            isGridView = uiState.preferences.isLibraryGrid,
                            onSelectTab = { viewModel.setLibraryTab(it) },
                            onToggleLayout = { viewModel.toggleLibraryGrid(it) },
                            onBookClick = { bookId -> viewModel.navigateTo(Screen.EbookReader(bookId)) },
                            onArticleClick = { articleId -> viewModel.navigateTo(Screen.ArticleReader(articleId)) },
                            onBookmarkBook = { book ->
                                viewModel.toggleBookmark(
                                    itemId = book.id,
                                    itemType = "BOOK",
                                    title = book.title,
                                    subtitle = book.author,
                                    category = "eBook"
                                )
                            },
                            onBookmarkArticle = { article ->
                                viewModel.toggleBookmark(
                                    itemId = article.id,
                                    itemType = "ARTICLE",
                                    title = article.title,
                                    subtitle = article.author,
                                    category = article.category
                                )
                            },
                            onExploreClick = { viewModel.navigateTo(Screen.Explore) },
                            onSearchClick = { viewModel.navigateTo(Screen.Search) },
                            onImportClick = { viewModel.navigateTo(Screen.Import) }
                        )
                    }

                    is Screen.Bookmarks -> {
                        BookmarksScreen(
                            bookmarks = uiState.bookmarks,
                            highlights = uiState.highlights,
                            onItemClick = { id, type ->
                                if (type == "BOOK") viewModel.navigateTo(Screen.EbookReader(id))
                                else viewModel.navigateTo(Screen.ArticleReader(id))
                            },
                            onRemoveBookmark = { id, itemId, itemType ->
                                viewModel.removeBookmark(id, itemId, itemType)
                            },
                            onDeleteHighlight = { id -> viewModel.deleteHighlight(id) },
                            onSearchClick = { viewModel.navigateTo(Screen.Search) }
                        )
                    }

                    is Screen.ArticleReader -> {
                        val article = uiState.articles.find { it.id == targetScreen.articleId }
                            ?: uiState.articles.firstOrNull()
                        if (article != null) {
                            ArticleReaderScreen(
                                article = article,
                                preferences = uiState.preferences,
                                onBackClick = { viewModel.navigateTo(Screen.Home) },
                                onBookmarkToggle = {
                                    viewModel.toggleBookmark(
                                        itemId = article.id,
                                        itemType = "ARTICLE",
                                        title = article.title,
                                        subtitle = article.author,
                                        category = article.category
                                    )
                                },
                                onOpenSettings = { viewModel.openReadingSettings() },
                                onAddHighlight = { text, note ->
                                    viewModel.addHighlight(
                                        contentId = article.id,
                                        contentType = "ARTICLE",
                                        title = article.title,
                                        text = text,
                                        note = note
                                    )
                                },
                                onUpdateArticleDetails = { id, title, author, category, summary, colorHex, coverUri ->
                                    viewModel.updateArticleDetails(id, title, author, category, summary, colorHex, coverUri)
                                }
                            )
                        }
                    }

                    is Screen.EbookReader -> {
                        val book = uiState.books.find { it.id == targetScreen.bookId }
                            ?: uiState.books.firstOrNull()
                        if (book != null) {
                            EbookReaderScreen(
                                book = book,
                                preferences = uiState.preferences,
                                onBackClick = { viewModel.navigateTo(Screen.Home) },
                                onBookmarkToggle = {
                                    viewModel.toggleBookmark(
                                        itemId = book.id,
                                        itemType = "BOOK",
                                        title = book.title,
                                        subtitle = book.author,
                                        category = "eBook"
                                    )
                                },
                                onOpenSettings = { viewModel.openReadingSettings() },
                                onAddHighlight = { text, note ->
                                    viewModel.addHighlight(
                                        contentId = book.id,
                                        contentType = "BOOK",
                                        title = book.title,
                                        text = text,
                                        note = note
                                    )
                                },
                                onUpdateProgress = { page, progress ->
                                    viewModel.updateBookProgress(book.id, page, progress)
                                },
                                onUpdateBookDetails = { id, title, author, category, synopsis, colorHex, coverUri ->
                                    viewModel.updateBookDetails(id, title, author, category, synopsis, colorHex, coverUri)
                                }
                            )
                        }
                    }

                    is Screen.Search -> {
                        SearchScreen(
                            searchQuery = uiState.searchQuery,
                            searchResults = uiState.searchResults,
                            onQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onBackClick = { viewModel.navigateTo(Screen.Home) },
                            onResultClick = { item ->
                                if (item.type == "eBook") viewModel.navigateTo(Screen.EbookReader(item.id))
                                else viewModel.navigateTo(Screen.ArticleReader(item.id))
                            }
                        )
                    }

                    is Screen.Profile -> {
                        ProfileScreen(
                            stats = uiState.readingStats,
                            preferences = uiState.preferences,
                            onUpdateProfile = { name, nickname, age, gender, uri, preset, bio ->
                                viewModel.updateUserProfile(name, nickname, age, gender, uri, preset, bio)
                            },
                            onUpdateThemeScheme = { viewModel.updateThemeScheme(it) },
                            onUpdateThemeMode = { viewModel.updateThemeMode(it) },
                            onToggleCompactMode = { viewModel.toggleCompactMode(it) },
                            onUpdatePaperTexture = { viewModel.updatePaperTexture(it) },
                            onUpdateFont = { viewModel.updateFont(it) },
                            onOpenStatistics = { viewModel.navigateTo(Screen.ReadingStatistics) },
                            onOpenSettings = { viewModel.openReadingSettings() },
                            onRemoveSampleData = { viewModel.removeSampleData() },
                            onRestoreSampleData = { viewModel.restoreSampleData() }
                        )
                    }

                    is Screen.ReadingStatistics -> {
                        ReadingStatisticsScreen(
                            stats = uiState.readingStats,
                            onBackClick = { viewModel.navigateTo(Screen.Profile) }
                        )
                    }

                    is Screen.Import -> {
                        ImportScreen(
                            onBackClick = { viewModel.navigateTo(Screen.Library) },
                            onImportSuccess = { parsedImport ->
                                viewModel.importParsedContent(parsedImport) { targetScreen ->
                                    viewModel.navigateTo(targetScreen)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Reading Settings Bottom Sheet
    if (uiState.isReadingSettingsOpen) {
        ReadingSettingsBottomSheet(
            preferences = uiState.preferences,
            onFontSizeChange = { viewModel.updateFontSize(it) },
            onFontChange = { viewModel.updateFont(it) },
            onLineSpacingChange = { viewModel.updateLineSpacing(it) },
            onPageThemeChange = { viewModel.updatePageTheme(it) },
            onBrightnessChange = { viewModel.updateBrightness(it) },
            onReadingWidthChange = { viewModel.updateReadingWidth(it) },
            onPaperTextureChange = { viewModel.updatePaperTexture(it) },
            onDismissRequest = { viewModel.closeReadingSettings() }
        )
    }
}

@Composable
fun MeyouBottomNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompact = com.example.ui.theme.LocalAppDimensions.current.isCompact

    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isCompact) 3.dp else 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen is Screen.Home,
            onClick = { onNavigate(Screen.Home) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", fontWeight = if (currentScreen is Screen.Home) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_home")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Explore,
            onClick = { onNavigate(Screen.Explore) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Explore) Icons.Filled.Explore else Icons.Outlined.Explore,
                    contentDescription = "Explore"
                )
            },
            label = { Text("Explore", fontWeight = if (currentScreen is Screen.Explore) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_explore")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Library,
            onClick = { onNavigate(Screen.Library) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Library) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = "Library"
                )
            },
            label = { Text("Library", fontWeight = if (currentScreen is Screen.Library) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_library")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Bookmarks,
            onClick = { onNavigate(Screen.Bookmarks) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Bookmarks) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmarks"
                )
            },
            label = { Text("Bookmarks", fontWeight = if (currentScreen is Screen.Bookmarks) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_bookmarks")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Profile,
            onClick = { onNavigate(Screen.Profile) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Profile) Icons.Filled.Person else Icons.Outlined.PersonOutline,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontWeight = if (currentScreen is Screen.Profile) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_profile")
        )
    }
}

// Retained for test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
