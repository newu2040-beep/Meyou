package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

enum class ImportType(val label: String) {
    EBOOK("eBook"),
    ARTICLE("Article")
}

data class ParsedImport(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String,
    val category: String,
    val content: String,
    val summaryOrSynopsis: String,
    val importType: ImportType,
    val estimatedPagesOrMinutes: Int,
    val accentColorHex: String = "#6750A4",
    val originalFileName: String? = null
)

object ContentImporter {

    fun parseFromUri(context: Context, uri: Uri): ParsedImport {
        val fileName = getFileName(context, uri) ?: "Imported_Document.txt"
        val extension = fileName.substringAfterLast('.', "").lowercase()

        var rawContent = ""
        var detectedTitle = fileName.substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        var detectedAuthor = "Imported Author"

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                when (extension) {
                    "epub" -> {
                        val epubResult = parseEpub(inputStream)
                        rawContent = epubResult.content
                        if (epubResult.title.isNotBlank()) detectedTitle = epubResult.title
                        if (epubResult.author.isNotBlank()) detectedAuthor = epubResult.author
                    }
                    "html", "htm" -> {
                        val htmlString = inputStream.bufferedReader().use { it.readText() }
                        val titleMatcher = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE).matcher(htmlString)
                        if (titleMatcher.find()) {
                            detectedTitle = cleanHtml(titleMatcher.group(1) ?: detectedTitle)
                        }
                        rawContent = cleanHtml(htmlString)
                    }
                    "json" -> {
                        val jsonString = inputStream.bufferedReader().use { it.readText() }
                        rawContent = jsonString
                    }
                    else -> {
                        // Default .txt, .md, text files
                        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                        val sb = StringBuilder()
                        var line: String? = reader.readLine()
                        var firstNonEmptyLine: String? = null

                        while (line != null) {
                            if (firstNonEmptyLine == null && line.isNotBlank()) {
                                firstNonEmptyLine = line.trim().removePrefix("#").trim()
                            }
                            sb.append(line).append("\n")
                            line = reader.readLine()
                        }
                        rawContent = sb.toString()

                        if (!firstNonEmptyLine.isNullOrBlank() && firstNonEmptyLine.length < 80) {
                            detectedTitle = firstNonEmptyLine
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            rawContent = "Unable to read file content: ${e.localizedMessage ?: "Unknown error"}"
        }

        return createParsedImport(
            title = detectedTitle,
            author = detectedAuthor,
            category = if (extension == "epub") "Classic Literature" else "General",
            content = rawContent.ifBlank { "Empty document." },
            originalFileName = fileName,
            forceType = if (extension == "epub") ImportType.EBOOK else null
        )
    }

    private fun parseEpub(inputStream: InputStream): EpubExtract {
        val zip = ZipInputStream(inputStream)
        var entry: ZipEntry? = zip.nextEntry
        val fullContent = StringBuilder()
        var title = ""
        var author = ""

        val htmlEntries = mutableListOf<Pair<String, String>>() // name to text

        while (entry != null) {
            val name = entry.name.lowercase()
            if (name.endsWith(".opf")) {
                val opfContent = zip.bufferedReader().readText()
                val titleMatcher = Pattern.compile("<dc:title[^>]*>(.*?)</dc:title>", Pattern.CASE_INSENSITIVE).matcher(opfContent)
                if (titleMatcher.find()) {
                    title = cleanHtml(titleMatcher.group(1) ?: "")
                }
                val authorMatcher = Pattern.compile("<dc:creator[^>]*>(.*?)</dc:creator>", Pattern.CASE_INSENSITIVE).matcher(opfContent)
                if (authorMatcher.find()) {
                    author = cleanHtml(authorMatcher.group(1) ?: "")
                }
            } else if (name.endsWith(".html") || name.endsWith(".xhtml") || name.endsWith(".htm")) {
                val text = zip.bufferedReader().readText()
                htmlEntries.add(Pair(entry.name, text))
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }

        // Sort entries by name to preserve chapter order
        htmlEntries.sortBy { it.first }
        for ((_, html) in htmlEntries) {
            val cleaned = cleanHtml(html)
            if (cleaned.isNotBlank()) {
                fullContent.append(cleaned).append("\n\n")
            }
        }

        return EpubExtract(
            title = title,
            author = author,
            content = fullContent.toString().trim()
        )
    }

    private data class EpubExtract(val title: String, val author: String, val content: String)

    private fun cleanHtml(html: String): String {
        return html
            .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("</div>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<h[1-6][^>]*>(.*?)</h[1-6]>", RegexOption.IGNORE_CASE), "\n\n$1\n\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            return cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
        return uri.path?.substringAfterLast('/')
    }

    fun createParsedImport(
        title: String,
        author: String,
        category: String,
        content: String,
        originalFileName: String? = null,
        forceType: ImportType? = null
    ): ParsedImport {
        val words = content.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size

        val type = forceType ?: if (wordCount > 4000 || content.contains("Chapter", ignoreCase = true) || content.contains("Book I", ignoreCase = true)) {
            ImportType.EBOOK
        } else {
            ImportType.ARTICLE
        }

        val estimatedPagesOrMinutes = if (type == ImportType.EBOOK) {
            (wordCount / 250).coerceAtLeast(1)
        } else {
            (wordCount / 200).coerceAtLeast(1)
        }

        val summary = if (words.size > 25) {
            words.take(35).joinToString(" ") + "..."
        } else {
            content.take(160)
        }

        val colors = listOf("#6750A4", "#7D5260", "#2E6B56", "#8E4E3A", "#3E5C76", "#5C5D72")
        val accentColor = colors[(title.hashCode() and 0x7FFFFFFF) % colors.size]

        return ParsedImport(
            title = title.trim().ifBlank { "Untitled Document" },
            author = author.trim().ifBlank { "Unknown Author" },
            category = category.trim().ifBlank { if (type == ImportType.EBOOK) "eBooks" else "Articles" },
            content = content.trim(),
            summaryOrSynopsis = summary,
            importType = type,
            estimatedPagesOrMinutes = estimatedPagesOrMinutes,
            accentColorHex = accentColor,
            originalFileName = originalFileName
        )
    }

    // Curated instant-import presets for offline testing and fast import
    val samplePresets = listOf(
        ParsedImport(
            id = "preset_metamorphosis",
            title = "The Metamorphosis",
            author = "Franz Kafka",
            category = "Classic Fiction",
            importType = ImportType.EBOOK,
            estimatedPagesOrMinutes = 72,
            accentColorHex = "#7356BF",
            summaryOrSynopsis = "One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed into a monstrous insect.",
            content = """
                CHAPTER I

                One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a monstrous insect. He lay on his armour-like back, and if he lifted his head a little he could see his brown belly, slightly domed and divided by arches into stiff sections. The bedding was hardly able to cover it and seemed ready to slide off any moment. His many legs, pitifully thin compared with the size of the rest of him, waved about helplessly as he looked.

                "What's happened to me?" he thought. It wasn't a dream. His room, a proper human room although a little too small, lay peacefully between its four familiar walls. A collection of textile samples lay spread out on the table—Samsa was a travelling salesman—and above it there hung a picture that he had recently cut out of an illustrated magazine and housed in a nice, gilded frame.

                Gregor then turned to look out the window at the dull weather. Drops of rain could be heard hitting the pane, which made him feel quite sad. "How about if I sleep a little bit longer and forget all this nonsense," he thought, but that was something he was unable to do because he was used to sleeping on his right, and in his present state he couldn't get into that position.

                CHAPTER II

                It was not until it was getting dark that evening that Gregor awoke from his deep and coma-like sleep. He was no sooner awake than he realized that it was not food he had been smelling, but a bowl of sweet milk in which small pieces of white bread were floating. He almost laughed with joy, as he was now even hungrier than he had been that morning, and he immediately dipped his head into the milk, nearly up to his eyes.

                He soon discovered that his left side seemed to be one single long, unpleasantly stretching scar, and he had to limp awkwardly on his two rows of legs. One of the legs had also been badly hurt during the morning's events, which seemed almost a miracle, and dragged along lifelessly.

                CHAPTER III

                Gregor's serious injury, which caused him to suffer for over a month—the apple remained embedded in his flesh as a visible reminder, as no one dared remove it—seemed to have reminded even his father that Gregor was a member of the family, in spite of his current, sad and repulsive appearance, and could not be treated as an enemy.

                He had grown accustomed to the darkness and silence of the room. He spent his days in quiet contemplation, watching the sunlight shift slowly across the wallpaper, listening to the muffled footsteps of family life continuing outside his door.
            """.trimIndent()
        ),
        ParsedImport(
            id = "preset_meditations",
            title = "Meditations: On Stillness & Clarity",
            author = "Marcus Aurelius",
            category = "Philosophy",
            importType = ImportType.EBOOK,
            estimatedPagesOrMinutes = 54,
            accentColorHex = "#4A6572",
            summaryOrSynopsis = "Timeless Stoic reflections on inner strength, focus, impermanence, and living with purpose.",
            content = """
                BOOK IV: THE INNER CITADEL

                Men seek retreats for themselves—in the country, by the sea, at the hills; and you too are especially wont to long after such things. But this is thoroughly common, since you have it in your power whenever you choose to retire into yourself. For nowhere can a man find a quieter or more untroubled retreat than in his own soul.

                Above all, constant resort to this retreat gives peace: by peace I mean nothing other than good order. Constantly give yourself this retreat, and renew yourself. Let your basic principles be brief and fundamental, such as will immediately wash away all pain and send you back without irritation to the affairs to which you must return.

                Consider how swiftly all things pass by and are forgotten: the boundless abyss of time past and future, the vanity of applause, the fickle nature of the human opinion, and the small space in which all this occurs. For the whole earth is a mere point, and how tiny a corner of it is your dwelling!

                Remember that tranquility is nothing more than good order of the mind. Clear your thoughts of grievances against fate, of fear regarding the future, and of anxiety over what others say or do. What others do is not in your control; what you choose to think and do is entirely your domain.

                Be like the headland on which the waves break unceasingly; it stands firm, and round it the seething waters are put to rest. "It is my bad luck that this has happened to me." No, you should say: "It is my good luck that, although this has happened to me, I can bear it without pain, neither crushed by the present nor fearful of the future."
            """.trimIndent()
        ),
        ParsedImport(
            id = "preset_deep_focus",
            title = "The Architecture of Unbroken Focus",
            author = "Dr. Julian Vance",
            category = "Productivity",
            importType = ImportType.ARTICLE,
            estimatedPagesOrMinutes = 6,
            accentColorHex = "#2E6B56",
            summaryOrSynopsis = "Why fractured attention is the silent productivity killer of modern work, and how cognitive insulation restores deep craft.",
            content = """
                In an era dominated by notification pings, infinite scroll feeds, and the constant pressure of asynchronous messaging, sustained attention has become a rare competitive superpower.

                The Cost of Attention Residue

                When you switch from task A to task B—even for just three seconds to glance at an email notification—your cognitive faculties do not switch cleanly. A portion of your working memory remains anchored to the previous context, a phenomenon cognitive psychologists term 'attention residue'.

                Over the course of an eight-hour workday punctuated by dozens of micro-interruptions, attention residue compounds. By mid-afternoon, you feel exhausted not because of the depth of your intellectual labor, but because your prefrontal cortex was repeatedly forced to reload context from scratch.

                Designing Cognitive Insulation

                To produce work of lasting value, we must engineer deliberate friction into our communication systems. Deep work requires prolonged periods—uninterrupted blocks of ninety minutes or more—where external stimuli cannot breach the boundary of your working canvas.

                Start by defining your primary reading and thinking hours. Protect them with the same uncompromising discipline an athlete brings to physical conditioning. When you immerse your mind into a single well-crafted text without interruption, you don't merely consume information—you forge neural pathways that foster high-order synthesis.
            """.trimIndent()
        )
    )
}
