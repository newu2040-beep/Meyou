package com.example.domain.model

data class Book(
    val id: String,
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
    val lastReadTimestamp: Long,
    val customCoverUri: String? = null
)

data class Article(
    val id: String,
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
    val progressPercent: Float,
    val lastReadTimestamp: Long,
    val customCoverUri: String? = null
)

data class BookmarkItem(
    val id: Long,
    val itemId: String,
    val itemType: String, // "BOOK", "ARTICLE", "HIGHLIGHT"
    val title: String,
    val subtitle: String,
    val category: String,
    val collectionName: String,
    val dateAdded: Long
)

data class HighlightItem(
    val id: Long,
    val contentId: String,
    val contentType: String,
    val title: String,
    val selectedText: String,
    val note: String,
    val colorHex: String,
    val sectionOrChapter: String,
    val timestamp: Long
)

data class ReadingStats(
    val totalReadingTimeMinutes: Int = 510,
    val booksCompleted: Int = 4,
    val articlesCompleted: Int = 19,
    val currentStreakDays: Int = 14,
    val averageDailyMinutes: Int = 38,
    val weeklyDayMinutes: List<Pair<String, Int>> = listOf(
        Pair("Mon", 45),
        Pair("Tue", 30),
        Pair("Wed", 60),
        Pair("Thu", 40),
        Pair("Fri", 50),
        Pair("Sat", 75),
        Pair("Sun", 35)
    )
) {
    val totalMinutesRead: Int get() = totalReadingTimeMinutes
    val booksFinished: Int get() = booksCompleted
    val articlesRead: Int get() = articlesCompleted
    val streakDays: Int get() = currentStreakDays
    val dailyAverageMinutes: Int get() = averageDailyMinutes
    val weeklyActivityMinutes: List<Int> get() = weeklyDayMinutes.map { it.second }
}
