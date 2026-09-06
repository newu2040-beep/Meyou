package com.example.data.repository

import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MeyouRepository(
    private val bookDao: BookDao,
    private val articleDao: ArticleDao,
    private val bookmarkDao: BookmarkDao,
    private val highlightDao: HighlightDao,
    private val readingDao: ReadingDao
) {
    constructor(database: com.example.data.local.database.MeyouDatabase) : this(
        database.bookDao(),
        database.articleDao(),
        database.bookmarkDao(),
        database.highlightDao(),
        database.readingDao()
    )

    val allBooks: Flow<List<Book>> = bookDao.getAllBooks().map { entities ->
        entities.map { it.toDomain() }
    }

    val allArticles: Flow<List<Article>> = articleDao.getAllArticles().map { entities ->
        entities.map { it.toDomain() }
    }

    val allBookmarks: Flow<List<BookmarkItem>> = bookmarkDao.getAllBookmarks().map { entities ->
        entities.map { it.toDomain() }
    }

    val allHighlights: Flow<List<HighlightItem>> = highlightDao.getAllHighlights().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getBookById(id: String): Flow<Book?> = bookDao.getBookById(id).map { it?.toDomain() }

    fun getArticleById(id: String): Flow<Article?> = articleDao.getArticleById(id).map { it?.toDomain() }

    fun getHighlightsForContent(contentId: String): Flow<List<HighlightItem>> =
        highlightDao.getHighlightsForContent(contentId).map { entities -> entities.map { it.toDomain() } }

    val readingStats: Flow<ReadingStats> = readingDao.getRecentSessions().map { sessions ->
        val totalMinutes = 480 + sessions.sumOf { it.durationMinutes }
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val weeklyData = days.map { day ->
            val match = sessions.filter { it.dayOfWeek.equals(day, ignoreCase = true) }.sumOf { it.durationMinutes }
            Pair(day, if (match > 0) match else (25..55).random())
        }
        ReadingStats(
            totalReadingTimeMinutes = totalMinutes,
            booksCompleted = 4,
            articlesCompleted = 19,
            currentStreakDays = 14,
            averageDailyMinutes = 38,
            weeklyDayMinutes = weeklyData
        )
    }

    suspend fun checkAndSeedInitialData() {
        val existingBooks = bookDao.getAllBooks().first()
        if (existingBooks.isEmpty()) {
            bookDao.insertBooks(InitialData.books)
            articleDao.insertArticles(InitialData.articles)
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    itemId = "book_atomic_habits",
                    itemType = "BOOK",
                    title = "Atomic Habits",
                    subtitle = "James Clear · Tiny Changes, Remarkable Results",
                    category = "eBooks"
                )
            )
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    itemId = "article_future_of_work",
                    itemType = "ARTICLE",
                    title = "The Future Belongs to People Who Keep Learning",
                    subtitle = "Elena Rostova · IDEAS",
                    category = "Articles"
                )
            )
            highlightDao.insertHighlight(
                HighlightEntity(
                    contentId = "book_atomic_habits",
                    contentType = "BOOK",
                    title = "Atomic Habits",
                    selectedText = "You do not rise to the level of your goals. You fall to the level of your systems.",
                    note = "Core concept for daily planning.",
                    colorHex = "#E8DEF8",
                    sectionOrChapter = "Chapter 1"
                )
            )
            highlightDao.insertHighlight(
                HighlightEntity(
                    contentId = "article_future_of_work",
                    contentType = "ARTICLE",
                    title = "The Future of Work",
                    selectedText = "Curiosity is the beginning of every meaningful change.",
                    note = "Mindset anchor",
                    colorHex = "#D4EDE2",
                    sectionOrChapter = "Section 2"
                )
            )
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            days.forEach { day ->
                readingDao.insertSession(
                    ReadingSessionEntity(
                        itemId = "book_atomic_habits",
                        itemType = "BOOK",
                        dayOfWeek = day,
                        durationMinutes = (30..50).random()
                    )
                )
            }
        }
    }

    suspend fun toggleBookmark(
        itemId: String,
        itemType: String,
        title: String,
        subtitle: String,
        category: String
    ): Boolean {
        val existingBookmarks = bookmarkDao.getAllBookmarks().first()
        val match = existingBookmarks.find { it.itemId == itemId }
        return if (match != null) {
            bookmarkDao.deleteById(match.id)
            if (itemType == "BOOK") bookDao.updateBookmark(itemId, false)
            else if (itemType == "ARTICLE") articleDao.updateBookmark(itemId, false)
            false
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    itemId = itemId,
                    itemType = itemType,
                    title = title,
                    subtitle = subtitle,
                    category = category
                )
            )
            if (itemType == "BOOK") bookDao.updateBookmark(itemId, true)
            else if (itemType == "ARTICLE") articleDao.updateBookmark(itemId, true)
            true
        }
    }

    suspend fun removeBookmark(id: Long, itemId: String, itemType: String) {
        bookmarkDao.deleteById(id)
        if (itemType == "BOOK") bookDao.updateBookmark(itemId, false)
        else if (itemType == "ARTICLE") articleDao.updateBookmark(itemId, false)
    }

    suspend fun updateBookProgress(bookId: String, page: Int, progress: Float) {
        bookDao.updateProgress(bookId, page, progress)
    }

    suspend fun updateArticleProgress(articleId: String, progress: Float) {
        articleDao.updateProgress(articleId, progress)
    }

    suspend fun addHighlight(
        contentId: String,
        contentType: String,
        title: String,
        text: String,
        note: String,
        colorHex: String,
        section: String
    ) {
        highlightDao.insertHighlight(
            HighlightEntity(
                contentId = contentId,
                contentType = contentType,
                title = title,
                selectedText = text,
                note = note,
                colorHex = colorHex,
                sectionOrChapter = section
            )
        )
    }

    suspend fun deleteHighlight(id: Long) {
        highlightDao.deleteHighlight(id)
    }

    suspend fun importBook(
        title: String,
        author: String,
        category: String,
        content: String,
        synopsis: String = "",
        totalPages: Int = 1,
        accentColorHex: String = "#6750A4"
    ): String {
        val id = "book_import_${System.currentTimeMillis()}"
        val entity = BookEntity(
            id = id,
            title = title,
            author = author,
            category = category,
            totalPages = totalPages.coerceAtLeast(1),
            currentPage = 1,
            progressPercent = 0f,
            currentChapter = "Chapter 1",
            synopsis = synopsis.ifBlank { "Imported eBook" },
            accentColorHex = accentColorHex,
            isBookmarked = false,
            isRecommended = false,
            isPopular = false,
            content = content,
            lastReadTimestamp = System.currentTimeMillis()
        )
        bookDao.insertBook(entity)
        return id
    }

    suspend fun importArticle(
        title: String,
        author: String,
        source: String,
        category: String,
        content: String,
        summary: String = "",
        readTimeMinutes: Int = 5,
        accentColorHex: String = "#6750A4"
    ): String {
        val id = "article_import_${System.currentTimeMillis()}"
        val entity = ArticleEntity(
            id = id,
            title = title,
            author = author,
            source = source.ifBlank { "Imported Document" },
            category = category,
            readTimeMinutes = readTimeMinutes.coerceAtLeast(1),
            publishDate = "Recently Imported",
            summary = summary.ifBlank { "Imported reading material" },
            featuredQuote = "",
            accentColorHex = accentColorHex,
            isBookmarked = false,
            isRecommended = false,
            isPopular = false,
            content = content,
            progressPercent = 0f,
            lastReadTimestamp = System.currentTimeMillis()
        )
        articleDao.insertArticle(entity)
        return id
    }

    suspend fun deleteBook(id: String) {
        bookDao.deleteBook(id)
        bookmarkDao.deleteByItemId(id)
    }

    suspend fun deleteArticle(id: String) {
        articleDao.deleteArticle(id)
        bookmarkDao.deleteByItemId(id)
    }
}

private fun BookEntity.toDomain() = Book(
    id = id,
    title = title,
    author = author,
    category = category,
    totalPages = totalPages,
    currentPage = currentPage,
    progressPercent = progressPercent,
    currentChapter = currentChapter,
    synopsis = synopsis,
    accentColorHex = accentColorHex,
    isBookmarked = isBookmarked,
    isRecommended = isRecommended,
    isPopular = isPopular,
    content = content,
    lastReadTimestamp = lastReadTimestamp
)

private fun ArticleEntity.toDomain() = Article(
    id = id,
    title = title,
    author = author,
    source = source,
    category = category,
    readTimeMinutes = readTimeMinutes,
    publishDate = publishDate,
    summary = summary,
    featuredQuote = featuredQuote,
    accentColorHex = accentColorHex,
    isBookmarked = isBookmarked,
    isRecommended = isRecommended,
    isPopular = isPopular,
    content = content,
    progressPercent = progressPercent,
    lastReadTimestamp = lastReadTimestamp
)

private fun BookmarkEntity.toDomain() = BookmarkItem(
    id = id,
    itemId = itemId,
    itemType = itemType,
    title = title,
    subtitle = subtitle,
    category = category,
    collectionName = collectionName,
    dateAdded = dateAdded
)

private fun HighlightEntity.toDomain() = HighlightItem(
    id = id,
    contentId = contentId,
    contentType = contentType,
    title = title,
    selectedText = selectedText,
    note = note,
    colorHex = colorHex,
    sectionOrChapter = sectionOrChapter,
    timestamp = timestamp
)

object InitialData {
    val books = listOf(
        BookEntity(
            id = "book_atomic_habits",
            title = "Atomic Habits",
            author = "James Clear",
            category = "eBooks",
            totalPages = 240,
            currentPage = 84,
            progressPercent = 0.35f,
            currentChapter = "Small Changes, Remarkable Results",
            synopsis = "An easy & proven way to build good habits & break bad ones.",
            accentColorHex = "#7356BF",
            isBookmarked = true,
            isRecommended = true,
            isPopular = true,
            content = """
                THE FUNDAMENTALS: Why Tiny Changes Make a Big Difference

                The fate of British Cycling changed one day in 2003. The organization, which was the governing body for professional cycling in Great Britain, had hired Dave Brailsford as its new performance director. At the time, professional cyclists in Great Britain had endured nearly one hundred years of mediocrity. Since 1908, British riders had won just a single gold medal at the Olympic Games, and they had fared even worse in cycling’s biggest race, the Tour de France. In 110 years, no British cyclist had ever won the event.

                Brailsford had been hired to put British Cycling on a new trajectory. What made him different from previous coaches was his relentless commitment to a strategy that he referred to as "the aggregation of marginal gains," which was the philosophy of searching for a tiny margin of improvement in everything you do. Brailsford said, "The whole principle came from the idea that if you broke down everything you could think of that goes into riding a bike, and then improve it by 1 percent, you will get a significant increase when you put them all together."

                They redesigned the bike seats to make them more comfortable and rubbed alcohol on the tires for a better grip. They asked riders to wear electrically heated overshorts to maintain ideal muscle temperature while riding and used wind-tunnel tests to test various fabrics on the riders.

                They searched for 1 percent improvements in overlooked areas: they tested different types of massage gels to see which led to the fastest muscle recovery. They hired a surgeon to teach each rider the best way to wash their hands to reduce the chances of catching a cold. They determined the type of pillow and mattress that led to the best night's sleep for each rider.

                Just five years after Brailsford took over, the British Cycling team dominated the road and track cycling events at the 2008 Olympic Games in Beijing, where they won an astounding 60 percent of the gold medals available.

                Why Small Habits Make a Big Difference

                It is so easy to overestimate the importance of one defining moment and underestimate the value of making small improvements on a daily basis. Too often, we convince ourselves that massive success requires massive action. Whether it is losing weight, building a business, writing a book, winning a championship, or achieving any other goal, we put pressure on ourselves to make some earth-shattering improvement that everyone will talk about.

                Meanwhile, improving by 1 percent isn’t particularly notable—sometimes it isn't even noticeable—but it can be far more meaningful, especially in the long run. The difference a tiny improvement can make over time is astounding. Here’s how the math works out: if you can get 1 percent better each day for one year, you’ll end up thirty-seven times better by the time you’re done. Conversely, if you get 1 percent worse each day for one year, you’ll decline nearly down to zero. What starts as a small win or a minor setback accumulates into something much more.

                Habits are the compound interest of self-improvement. The same way that money multiplies through compound interest, the effects of your habits multiply as you repeat them. They seem to make little difference on any given day and yet the impact they deliver over the months and years can be enormous. It is only when looking back two, five, or perhaps ten years later that the value of good habits and the cost of bad ones becomes strikingly apparent.

                You do not rise to the level of your goals. You fall to the level of your systems.
            """.trimIndent()
        ),
        BookEntity(
            id = "book_psychology_of_money",
            title = "The Psychology of Money",
            author = "Morgan Housel",
            category = "eBooks",
            totalPages = 256,
            currentPage = 38,
            progressPercent = 0.15f,
            currentChapter = "No One's Crazy",
            synopsis = "Timeless lessons on wealth, greed, and happiness doing well with money.",
            accentColorHex = "#8E6ABF",
            isBookmarked = false,
            isRecommended = true,
            isPopular = true,
            content = """
                NO ONE'S CRAZY: Your personal experiences with money make up maybe 0.00000001% of what’s happened in the world, but maybe 80% of how you think the world works.

                Every decision people make with money is justified by taking the information they have at the moment and plugging it into their unique mental model of how the world works.

                Those people can be misinformed. They can have incomplete information. They can be bad at math. They can be swayed by rotten marketing. They can have no idea what they are doing. But every decision they make makes sense to them in that moment.

                Tell someone about a 30-year bond and they will look at you like you are speaking Greek if they grew up during hyperinflation. Offer someone index funds who came of age during the 1970s stagflation and they will tell you stocks are a scam.

                We all make decisions based on our own experiences that seem to make sense to us in a given moment.
            """.trimIndent()
        ),
        BookEntity(
            id = "book_deep_work",
            title = "Deep Work",
            author = "Cal Newport",
            category = "eBooks",
            totalPages = 304,
            currentPage = 1,
            progressPercent = 0.0f,
            currentChapter = "The Deep Work Hypothesis",
            synopsis = "Rules for focused success in a distracted world.",
            accentColorHex = "#5E4A8C",
            isBookmarked = false,
            isRecommended = true,
            isPopular = true,
            content = """
                THE DEEP WORK HYPOTHESIS: The ability to perform deep work is becoming increasingly rare at exactly the same time it is becoming increasingly valuable in our economy.

                As a consequence, the few who cultivate this skill, and then make it the core of their working life, will thrive.

                Deep work is not some nostalgic affectation of writers and early-twentieth-century philosophers. It is instead a skill that has great value today.

                To produce at your peak level you need to work for extended periods with full concentration on a single task free from distraction. Put another way, the type of work that optimizes your performance is deep work.
            """.trimIndent()
        ),
        BookEntity(
            id = "book_digital_minimalism",
            title = "Digital Minimalism",
            author = "Cal Newport",
            category = "eBooks",
            totalPages = 245,
            currentPage = 172,
            progressPercent = 0.70f,
            currentChapter = "A Modern Luddite",
            synopsis = "Choosing a focused life in a noisy world.",
            accentColorHex = "#7A68A6",
            isBookmarked = false,
            isRecommended = true,
            isPopular = false,
            content = """
                A PHILOSOPHY OF TECHNOLOGY USE: In which you focus your online time on a small number of carefully selected and optimized activities that strongly support things you value.

                Digital minimalists see new technologies as tools to be used to support things they deeply value—not as sources of value themselves. They don't accept the premise that offering some benefit is enough to justify using a technology.

                Instead, they are aggressive about pruning away the low-value digital noise that clutters their lives and leaves them feeling exhausted and drained.
            """.trimIndent()
        )
    )

    val articles = listOf(
        ArticleEntity(
            id = "article_future_of_work",
            title = "The Future Belongs to People Who Keep Learning",
            author = "Elena Rostova",
            source = "IDEAS",
            category = "Technology",
            readTimeMinutes = 8,
            publishDate = "September 2026",
            summary = "Why deep adaptability and continuous curiosity are the highest-leverage human capabilities in an automated world.",
            featuredQuote = "Curiosity is the beginning of every meaningful change.",
            accentColorHex = "#7356BF",
            isBookmarked = true,
            isRecommended = true,
            isPopular = true,
            content = """
                We are living through a profound inflection point in human labor. In every industry, machines and algorithmic models are rapidly mastering routine, predictable patterns of calculation and synthesis.

                Yet far from rendering human insight obsolete, this transition is highlighting the irreplaceable nature of genuine curiosity, critical judgment, and cross-disciplinary empathy. The people who will thrive in the coming decades are not those who memorized a single rigid playbook, but those who mastered the quiet art of continuous self-renewal.

                The Illusion of Static Expertise

                For centuries, professional mastery followed a predictable trajectory: invest several years in formal education, acquire a specialized vocational toolkit, and apply those principles until retirement. Today, the halflife of technical knowledge is shrinking to fewer than three years.

                To rely on what you already know is akin to standing still on an upward-moving escalator. True durability comes not from the weight of your answers, but from the velocity with which you can formulate new, generative questions.

                Curiosity is the beginning of every meaningful change.

                When you approach complex, ambiguous problems with genuine wonder rather than defensive skepticism, obstacles become instructive signals. Curiosity reconfigures failure from a verdict on your competence into high-fidelity feedback.

                The Craft of Focused Reading

                In an age characterized by fractured attention spans and algorithmic feeds engineered for instant dopamine, deliberate reading is an act of radical rebellion. When you read a comprehensive article or a deeply considered book, you are not merely absorbing information—you are borrowing the refined cognitive architecture of another human being.

                You teach your mind to sustain attention through nuance, to synthesize counterintuitive paradoxes, and to construct internal frameworks that outlast ephemeral headlines.

                Building Your Personal Learning Engine

                To cultivate lifelong adaptability, treat your intellect as an organic garden rather than a static warehouse:

                1. Protect uninterrupted focus daily: Guard 60 to 90 minutes of sacred time free from alerts, notifications, or multitasking.
                2. Read across boundaries: Combine history with software architecture, philosophy with cognitive biology. The most potent breakthroughs always occur at the intersections.
                3. Annotate and synthesize: Don't let profound insights slip away. Highlight sentences that resonate, jot down counterarguments in the margins, and translate concepts into your own voice.

                The future does not belong to the loudest or the fastest. It belongs to those who stay curious, read deeply, and never cease to learn.
            """.trimIndent()
        ),
        ArticleEntity(
            id = "article_science_of_happiness",
            title = "The Science of Happiness",
            author = "Dr. Arthur Brooks",
            source = "MEYOU MIND",
            category = "Mindset",
            readTimeMinutes = 6,
            publishDate = "August 2026",
            summary = "How to build a more meaningful life by aligning purpose, enjoyment, and emotional balance.",
            featuredQuote = "Happiness is not a destination you arrive at, but a direction you choose to walk.",
            accentColorHex = "#8E6ABF",
            isBookmarked = false,
            isRecommended = true,
            isPopular = true,
            content = """
                Many people mistake happiness for an uninterrupted state of euphoria. In reality, psychological well-being is composed of three distinct dimensions: enjoyment, satisfaction, and purpose.

                Enjoyment is not mere pleasure; it requires active engagement and memory creation. Satisfaction is the reward of intentional effort and delayed gratification. Purpose is the underlying belief that your presence and actions matter to others.

                When you balance these three pillars, emotional equilibrium naturally follows.
            """.trimIndent()
        ),
        ArticleEntity(
            id = "article_future_work_flexibility",
            title = "The Future of Work",
            author = "Sarah Jenkins",
            source = "FUTURE",
            category = "Business",
            readTimeMinutes = 9,
            publishDate = "August 2026",
            summary = "Why flexibility is becoming the new freedom for knowledge creators worldwide.",
            featuredQuote = "Autonomy over your calendar is the ultimate measure of wealth.",
            accentColorHex = "#5E4A8C",
            isBookmarked = false,
            isRecommended = true,
            isPopular = true,
            content = """
                The modern workplace is moving away from the industrial model of synchronized physical attendance. Creative professionals increasingly measure their quality of life by temporal autonomy—the liberty to read, think, and execute when their minds are sharpest.

                Async collaboration and remote craftsmanship allow deep, uninterrupted blocks for reading and strategic synthesis, fostering healthier, more resilient teams.
            """.trimIndent()
        ),
        ArticleEntity(
            id = "article_better_sleep",
            title = "Better Sleep, Better You",
            author = "Dr. Matthew Walker",
            source = "WELLBEING",
            category = "Health",
            readTimeMinutes = 7,
            publishDate = "July 2026",
            summary = "Simple habits for a healthier mind and sustained cognitive performance.",
            featuredQuote = "Sleep is the single most effective thing we can do each day to reset our brain and body health.",
            accentColorHex = "#7A68A6",
            isBookmarked = false,
            isRecommended = true,
            isPopular = true,
            content = """
                Sleep is the foundational substrate of cognitive resilience. During non-REM deep sleep, the brain consolidates memories, clears metabolic waste, and prepares neural pathways for learning new concepts.

                Replacing late-night blue-light screen exposure with 30 minutes of reading warm, non-glare typography has been clinically shown to decrease sleep latency and boost deep-sleep duration by over 20 percent.
            """.trimIndent()
        ),
        ArticleEntity(
            id = "article_small_steps",
            title = "The Power of Small Steps",
            author = "James Clear",
            source = "CULTURE",
            category = "Mindset",
            readTimeMinutes = 5,
            publishDate = "June 2026",
            summary = "Tiny changes, big results: how incremental momentum reshapes identity.",
            featuredQuote = "Every action you take is a vote for the type of person you wish to become.",
            accentColorHex = "#7356BF",
            isBookmarked = false,
            isRecommended = true,
            isPopular = true,
            content = """
                We often look for dramatic transformations and radical breakthroughs. But the real architecture of mastery is quietly built through microscopic, repeatable behaviors.

                Reading just 15 pages a day results in over 20 completed books each year. Over a decade, that is two hundred books of accumulated wisdom, transforming your judgment and creative outlook.
            """.trimIndent()
        )
    )
}
