package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.datastore.ReadingFont
import com.example.data.datastore.ReadingPageTheme
import com.example.data.datastore.UserPreferencesRepository
import com.example.data.local.database.MeyouDatabase
import com.example.data.repository.MeyouRepository
import com.example.ui.viewmodel.MeyouViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MeyouRobolectricTest {

    private lateinit var database: MeyouDatabase
    private lateinit var repository: MeyouRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var context: Context
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, MeyouDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MeyouRepository(database)
        preferencesRepository = UserPreferencesRepository(context)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial data seeding populates books and articles`() = runTest {
        repository.checkAndSeedInitialData()

        val books = repository.allBooks.first()
        val articles = repository.allArticles.first()

        assertTrue("Books should not be empty after seeding", books.isNotEmpty())
        assertTrue("Articles should not be empty after seeding", articles.isNotEmpty())

        val atomicHabits = books.find { it.id == "book_atomic_habits" }
        assertNotNull(atomicHabits)
        assertEquals("Atomic Habits", atomicHabits?.title)
        assertEquals("James Clear", atomicHabits?.author)
    }

    @Test
    fun `bookmark toggle works correctly`() = runTest {
        repository.checkAndSeedInitialData()

        // Toggle bookmark for an unbookmarked item
        val added = repository.toggleBookmark(
            itemId = "article_science_of_happiness",
            itemType = "ARTICLE",
            title = "The Science of Everyday Happiness",
            subtitle = "By Dr. Maya Lin",
            category = "Health"
        )
        assertTrue("Bookmark should be added", added)

        val bookmarks = repository.allBookmarks.first()
        val bookmarkedItem = bookmarks.find { it.itemId == "article_science_of_happiness" }
        assertNotNull(bookmarkedItem)

        // Toggle again to remove
        val removed = repository.toggleBookmark(
            itemId = "article_science_of_happiness",
            itemType = "ARTICLE",
            title = "The Science of Everyday Happiness",
            subtitle = "By Dr. Maya Lin",
            category = "Health"
        )
        assertFalse("Bookmark should be removed on second toggle", removed)
    }

    @Test
    fun `highlight creation and deletion persists properly`() = runTest {
        repository.addHighlight(
            contentId = "book_atomic_habits",
            contentType = "BOOK",
            title = "Atomic Habits",
            text = "You do not rise to the level of your goals. You fall to the level of your systems.",
            note = "Key insight on systems",
            colorHex = "#E8DEF8",
            section = "Chapter 1"
        )

        val highlights = repository.allHighlights.first()
        assertTrue("Highlight list should contain saved quote", highlights.isNotEmpty())
        val saved = highlights.first()
        assertEquals("Atomic Habits", saved.title)
        assertEquals("Key insight on systems", saved.note)

        repository.deleteHighlight(saved.id)
        val afterDelete = repository.allHighlights.first()
        assertTrue("Highlight should be deleted", afterDelete.isEmpty())
    }

    @Test
    fun `preferences updates persist font and page theme`() = runTest {
        preferencesRepository.updateFontSize(22f)
        preferencesRepository.updateFont(ReadingFont.SERIF)
        preferencesRepository.updatePageTheme(ReadingPageTheme.LAVENDER)

        val prefs = preferencesRepository.preferencesFlow.first()
        assertEquals(22f, prefs.fontSizeSp)
        assertEquals(ReadingFont.SERIF, prefs.font)
        assertEquals(ReadingPageTheme.LAVENDER, prefs.pageTheme)
    }

    @Test
    fun `viewModel navigation and search behave as expected`() = runTest {
        repository.checkAndSeedInitialData()
        val viewModel = MeyouViewModel(repository, preferencesRepository)

        viewModel.navigateTo(Screen.Explore)
        assertEquals(Screen.Explore, viewModel.uiState.value.currentScreen)

        viewModel.navigateTo(Screen.Library)
        assertEquals(Screen.Library, viewModel.uiState.value.currentScreen)

        viewModel.onSearchQueryChanged("Habits")
        val searchResults = viewModel.uiState.value.searchResults
        assertTrue("Search should return results for 'Habits'", searchResults.isNotEmpty())
    }

    @Test
    fun `importBook and importArticle insert and delete properly`() = runTest {
        val bookId = repository.importBook(
            title = "Test Imported Book",
            author = "Test Author",
            category = "Philosophy",
            content = "This is a test book content with multiple sentences for testing.",
            synopsis = "A test book synopsis",
            totalPages = 42,
            accentColorHex = "#7986CB"
        )
        assertTrue(bookId.isNotBlank())

        val books = repository.allBooks.first()
        val importedBook = books.find { it.id == bookId }
        assertNotNull(importedBook)
        assertEquals("Test Imported Book", importedBook?.title)
        assertEquals("Test Author", importedBook?.author)

        // Delete book
        repository.deleteBook(bookId)
        val afterDeleteBooks = repository.allBooks.first()
        assertNull(afterDeleteBooks.find { it.id == bookId })

        // Article import
        val articleId = repository.importArticle(
            title = "Test Imported Article",
            author = "Test Essayist",
            source = "My Notes",
            category = "Technology",
            content = "A brief essay on technology and humanity.",
            summary = "Tech summary",
            readTimeMinutes = 5,
            accentColorHex = "#4DB6AC"
        )
        val articles = repository.allArticles.first()
        val importedArticle = articles.find { it.id == articleId }
        assertNotNull(importedArticle)
        assertEquals("Test Imported Article", importedArticle?.title)

        // Delete article
        repository.deleteArticle(articleId)
        val afterDeleteArticles = repository.allArticles.first()
        assertNull(afterDeleteArticles.find { it.id == articleId })
    }

    @Test
    fun `contentImporter creates parsed import correctly`() {
        val sample = """
            Silence is not merely the absence of acoustic sound.
            It is the positive presence of focused contemplation.
            It grants the intellect room to synthesize deep understanding.
        """.trimIndent()

        val parsed = com.example.util.ContentImporter.createParsedImport(
            title = "The Architecture of Silence",
            author = "Elena Rostova",
            category = "Philosophy",
            content = sample,
            forceType = com.example.util.ImportType.ARTICLE
        )

        assertEquals("The Architecture of Silence", parsed.title)
        assertEquals("Elena Rostova", parsed.author)
        assertEquals(com.example.util.ImportType.ARTICLE, parsed.importType)
        assertTrue(parsed.content.contains("Silence is not merely"))
        assertTrue(parsed.estimatedPagesOrMinutes >= 1)
    }
}
