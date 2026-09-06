package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.*
import com.example.data.repository.MeyouRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Library : Screen("library")
    object Bookmarks : Screen("bookmarks")
    data class ArticleReader(val articleId: String) : Screen("article_reader/$articleId")
    data class EbookReader(val bookId: String) : Screen("ebook_reader/$bookId")
    object Search : Screen("search")
    object Profile : Screen("profile")
    object ReadingStatistics : Screen("statistics")
    object Import : Screen("import")
}

data class MeyouUiState(
    val currentScreen: Screen = Screen.Home,
    val books: List<Book> = emptyList(),
    val articles: List<Article> = emptyList(),
    val bookmarks: List<BookmarkItem> = emptyList(),
    val highlights: List<HighlightItem> = emptyList(),
    val readingStats: ReadingStats = ReadingStats(510, 4, 19, 14, 38, emptyList()),
    val preferences: ReadingPreferences = ReadingPreferences(),
    val selectedHomeCategory: String = "All",
    val selectedExploreTopic: String = "For You",
    val selectedLibraryTab: String = "All",
    val searchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    val isReadingSettingsOpen: Boolean = false,
    val isLoading: Boolean = false,
    val userSnackbarMessage: String? = null
)

data class SearchResultItem(
    val id: String,
    val title: String,
    val author: String,
    val type: String, // "eBook" or "Article"
    val subtitle: String,
    val readingTimeOrPages: String,
    val isBookmarked: Boolean
)

class MeyouViewModel(
    private val repository: MeyouRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeyouUiState(isLoading = true))
    val uiState: StateFlow<MeyouUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }

        viewModelScope.launch {
            repository.allBooks.collect { books ->
                _uiState.update { it.copy(books = books) }
            }
        }

        viewModelScope.launch {
            repository.allArticles.collect { articles ->
                _uiState.update { it.copy(articles = articles) }
            }
        }

        viewModelScope.launch {
            repository.allBookmarks.collect { bookmarks ->
                _uiState.update { it.copy(bookmarks = bookmarks) }
            }
        }

        viewModelScope.launch {
            repository.allHighlights.collect { highlights ->
                _uiState.update { it.copy(highlights = highlights) }
            }
        }

        viewModelScope.launch {
            repository.readingStats.collect { stats ->
                _uiState.update { it.copy(readingStats = stats) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.update { current ->
                    val screen = if (!prefs.isOnboardingComplete && current.currentScreen is Screen.Home) {
                        Screen.Onboarding
                    } else current.currentScreen
                    current.copy(preferences = prefs, currentScreen = screen, isLoading = false)
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setOnboardingComplete() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingComplete(true)
            _uiState.update { it.copy(currentScreen = Screen.Home) }
        }
    }

    fun setHomeCategory(category: String) {
        _uiState.update { it.copy(selectedHomeCategory = category) }
    }

    fun setExploreTopic(topic: String) {
        _uiState.update { it.copy(selectedExploreTopic = topic) }
    }

    fun setLibraryTab(tab: String) {
        _uiState.update { it.copy(selectedLibraryTab = tab) }
    }

    fun toggleLibraryGrid(isGrid: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setLibraryGrid(isGrid)
        }
    }

    fun openReadingSettings() {
        _uiState.update { it.copy(isReadingSettingsOpen = true) }
    }

    fun closeReadingSettings() {
        _uiState.update { it.copy(isReadingSettingsOpen = false) }
    }

    fun updateFontSize(size: Float) {
        viewModelScope.launch { preferencesRepository.updateFontSize(size) }
    }

    fun updateFont(font: ReadingFont) {
        viewModelScope.launch { preferencesRepository.updateFont(font) }
    }

    fun updateLineSpacing(spacing: Float) {
        viewModelScope.launch { preferencesRepository.updateLineSpacing(spacing) }
    }

    fun updatePageTheme(theme: ReadingPageTheme) {
        viewModelScope.launch { preferencesRepository.updatePageTheme(theme) }
    }

    fun updateBrightness(brightness: Float) {
        viewModelScope.launch { preferencesRepository.updateBrightness(brightness) }
    }

    fun updateReadingWidth(width: ReadingWidth) {
        viewModelScope.launch { preferencesRepository.updateReadingWidth(width) }
    }

    fun toggleBookmark(itemId: String, itemType: String, title: String, subtitle: String, category: String) {
        viewModelScope.launch {
            val added = repository.toggleBookmark(itemId, itemType, title, subtitle, category)
            _uiState.update {
                it.copy(
                    userSnackbarMessage = if (added) "Saved to bookmarks" else "Removed from bookmarks"
                )
            }
        }
    }

    fun removeBookmark(id: Long, itemId: String, itemType: String) {
        viewModelScope.launch {
            repository.removeBookmark(id, itemId, itemType)
            _uiState.update { it.copy(userSnackbarMessage = "Bookmark removed") }
        }
    }

    fun updateBookProgress(bookId: String, page: Int, progress: Float) {
        viewModelScope.launch {
            repository.updateBookProgress(bookId, page, progress)
        }
    }

    fun updateArticleProgress(articleId: String, progress: Float) {
        viewModelScope.launch {
            repository.updateArticleProgress(articleId, progress)
        }
    }

    fun addHighlight(contentId: String, contentType: String, title: String, text: String, note: String = "") {
        viewModelScope.launch {
            repository.addHighlight(contentId, contentType, title, text, note, "#E8DEF8", "Current Page")
            _uiState.update { it.copy(userSnackbarMessage = "Highlight saved") }
        }
    }

    fun deleteHighlight(id: Long) {
        viewModelScope.launch {
            repository.deleteHighlight(id)
            _uiState.update { it.copy(userSnackbarMessage = "Highlight removed") }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val results = if (query.isBlank()) {
                emptyList()
            } else {
                val bookResults = current.books.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true) ||
                    it.synopsis.contains(query, ignoreCase = true)
                }.map {
                    SearchResultItem(
                        id = it.id,
                        title = it.title,
                        author = it.author,
                        type = "eBook",
                        subtitle = it.currentChapter,
                        readingTimeOrPages = "${it.totalPages} pages",
                        isBookmarked = it.isBookmarked
                    )
                }

                val articleResults = current.articles.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true) ||
                    it.summary.contains(query, ignoreCase = true)
                }.map {
                    SearchResultItem(
                        id = it.id,
                        title = it.title,
                        author = it.author,
                        type = "Article",
                        subtitle = "${it.source} · ${it.category}",
                        readingTimeOrPages = "${it.readTimeMinutes} min read",
                        isBookmarked = it.isBookmarked
                    )
                }

                bookResults + articleResults
            }
            current.copy(searchQuery = query, searchResults = results)
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(userSnackbarMessage = null) }
    }

    fun importParsedContent(parsed: com.example.util.ParsedImport, onComplete: ((Screen) -> Unit)? = null) {
        viewModelScope.launch {
            val targetScreen = if (parsed.importType == com.example.util.ImportType.EBOOK) {
                val bookId = repository.importBook(
                    title = parsed.title,
                    author = parsed.author,
                    category = parsed.category,
                    content = parsed.content,
                    synopsis = parsed.summaryOrSynopsis,
                    totalPages = parsed.estimatedPagesOrMinutes,
                    accentColorHex = parsed.accentColorHex
                )
                _uiState.update { it.copy(userSnackbarMessage = "Imported '${parsed.title}' as eBook") }
                Screen.EbookReader(bookId)
            } else {
                val articleId = repository.importArticle(
                    title = parsed.title,
                    author = parsed.author,
                    source = parsed.originalFileName ?: "Imported Article",
                    category = parsed.category,
                    content = parsed.content,
                    summary = parsed.summaryOrSynopsis,
                    readTimeMinutes = parsed.estimatedPagesOrMinutes,
                    accentColorHex = parsed.accentColorHex
                )
                _uiState.update { it.copy(userSnackbarMessage = "Imported '${parsed.title}' as Article") }
                Screen.ArticleReader(articleId)
            }
            onComplete?.invoke(targetScreen)
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch {
            repository.deleteBook(id)
            _uiState.update { it.copy(userSnackbarMessage = "eBook removed") }
        }
    }

    fun deleteArticle(id: String) {
        viewModelScope.launch {
            repository.deleteArticle(id)
            _uiState.update { it.copy(userSnackbarMessage = "Article removed") }
        }
    }
}

class MeyouViewModelFactory(
    private val repository: MeyouRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeyouViewModel::class.java)) {
            return MeyouViewModel(repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
