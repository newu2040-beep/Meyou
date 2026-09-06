package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastReadTimestamp DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE isRecommended = 1")
    fun getRecommendedBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isPopular = 1")
    fun getPopularBooks(): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: String)

    @Query("DELETE FROM books WHERE id LIKE 'book_%'")
    suspend fun deleteSampleBooks()

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET title = :title, author = :author, category = :category, synopsis = :synopsis, accentColorHex = :accentColorHex, customCoverUri = :customCoverUri WHERE id = :id")
    suspend fun updateBookDetails(id: String, title: String, author: String, category: String, synopsis: String, accentColorHex: String, customCoverUri: String?)

    @Query("UPDATE books SET customCoverUri = :customCoverUri WHERE id = :id")
    suspend fun updateBookCover(id: String, customCoverUri: String?)

    @Query("UPDATE books SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, bookmarked: Boolean)

    @Query("UPDATE books SET currentPage = :page, progressPercent = :progress, lastReadTimestamp = :time WHERE id = :id")
    suspend fun updateProgress(id: String, page: Int, progress: Float, time: Long = System.currentTimeMillis())
}

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY lastReadTimestamp DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun getArticleById(id: String): Flow<ArticleEntity?>

    @Query("SELECT * FROM articles WHERE isRecommended = 1")
    fun getRecommendedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE category = :category")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteArticle(id: String)

    @Query("DELETE FROM articles WHERE id LIKE 'article_%'")
    suspend fun deleteSampleArticles()

    @Query("DELETE FROM articles")
    suspend fun deleteAllArticles()

    @Update
    suspend fun updateArticle(article: ArticleEntity)

    @Query("UPDATE articles SET title = :title, author = :author, category = :category, summary = :summary, accentColorHex = :accentColorHex, customCoverUri = :customCoverUri WHERE id = :id")
    suspend fun updateArticleDetails(id: String, title: String, author: String, category: String, summary: String, accentColorHex: String, customCoverUri: String?)

    @Query("UPDATE articles SET customCoverUri = :customCoverUri WHERE id = :id")
    suspend fun updateArticleCover(id: String, customCoverUri: String?)

    @Query("UPDATE articles SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, bookmarked: Boolean)

    @Query("UPDATE articles SET progressPercent = :progress, lastReadTimestamp = :time WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Float, time: Long = System.currentTimeMillis())
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY dateAdded DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE itemType = :type ORDER BY dateAdded DESC")
    fun getBookmarksByType(type: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: String)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights ORDER BY timestamp DESC")
    fun getAllHighlights(): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE contentId = :contentId ORDER BY timestamp DESC")
    fun getHighlightsForContent(contentId: String): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE id = :id")
    suspend fun deleteHighlight(id: Long)
}

@Dao
interface ReadingDao {
    @Query("SELECT * FROM reading_sessions ORDER BY timestamp DESC LIMIT 7")
    fun getRecentSessions(): Flow<List<ReadingSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSessionEntity)
}
