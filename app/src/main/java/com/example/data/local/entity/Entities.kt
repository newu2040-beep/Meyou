package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val category: String,
    val totalPages: Int,
    val currentPage: Int,
    val progressPercent: Float,
    val currentChapter: String,
    val synopsis: String,
    val accentColorHex: String,
    val isBookmarked: Boolean,
    val isRecommended: Boolean,
    val isPopular: Boolean,
    val content: String,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val customCoverUri: String? = null
)

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val source: String,
    val category: String,
    val readTimeMinutes: Int,
    val publishDate: String,
    val summary: String,
    val featuredQuote: String,
    val accentColorHex: String,
    val isBookmarked: Boolean,
    val isRecommended: Boolean,
    val isPopular: Boolean,
    val content: String,
    val progressPercent: Float = 0f,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val customCoverUri: String? = null
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: String,
    val itemType: String, // "BOOK", "ARTICLE", "HIGHLIGHT"
    val title: String,
    val subtitle: String,
    val category: String,
    val collectionName: String = "General",
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentId: String,
    val contentType: String,
    val title: String,
    val selectedText: String,
    val note: String = "",
    val colorHex: String = "#E8DEF8",
    val sectionOrChapter: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_sessions")
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: String,
    val itemType: String,
    val dayOfWeek: String, // "Mon", "Tue", etc.
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
