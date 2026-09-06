package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.domain.model.Article
import com.example.domain.model.Book
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class ExportFormat(val extension: String, val mimeType: String, val label: String, val iconLabel: String) {
    PDF("pdf", "application/pdf", "PDF Document (.pdf)", "PDF"),
    TXT("txt", "text/plain", "Plain Text (.txt)", "TXT"),
    CSV("csv", "text/csv", "Spreadsheet / CSV (.csv)", "CSV"),
    JSON("json", "application/json", "Structured Data (.json)", "JSON")
}

object ShareExportHelper {

    fun exportAndShareBook(
        context: Context,
        book: Book,
        format: ExportFormat
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanTitle = sanitizeFileName(book.title)
            val file = File(exportDir, "${cleanTitle}_meyou.${format.extension}")

            when (format) {
                ExportFormat.PDF -> generateBookPdf(book, file)
                ExportFormat.TXT -> generateBookTxt(book, file)
                ExportFormat.CSV -> generateBookCsv(book, file)
                ExportFormat.JSON -> generateBookJson(book, file)
            }

            shareFileIntent(context, file, format.mimeType, "Share \"${book.title}\" via")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportAndShareArticle(
        context: Context,
        article: Article,
        format: ExportFormat
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanTitle = sanitizeFileName(article.title)
            val file = File(exportDir, "${cleanTitle}_meyou.${format.extension}")

            when (format) {
                ExportFormat.PDF -> generateArticlePdf(article, file)
                ExportFormat.TXT -> generateArticleTxt(article, file)
                ExportFormat.CSV -> generateArticleCsv(article, file)
                ExportFormat.JSON -> generateArticleJson(article, file)
            }

            shareFileIntent(context, file, format.mimeType, "Share \"${article.title}\" via")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFileIntent(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
            putExtra(Intent.EXTRA_TEXT, "Shared from MEYOU Reader")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    // -------------------------------------------------------------
    // PDF Generation
    // -------------------------------------------------------------
    private fun generateBookPdf(book: Book, outputFile: File) {
        val document = PdfDocument()
        val pageWidth = 595 // A4 standard pt
        val pageHeight = 842
        val margin = 48f

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val authorPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paragraphs = book.content.split(Regex("(\r?\n)+")).filter { it.isNotBlank() }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var yPos = margin + 20f

        // Draw Title & Author on Page 1
        canvas.drawText(book.title, margin, yPos, titlePaint)
        yPos += 24f
        canvas.drawText("Author: ${book.author} · Category: ${book.category}", margin, yPos, authorPaint)
        yPos += 16f
        canvas.drawText("Reading Progress: ${(book.progressPercent * 100).toInt()}% · MEYOU Expressive Reader", margin, yPos, authorPaint)
        yPos += 24f

        val textWidth = pageWidth - 2 * margin
        val lineHeight = 16f

        for (paragraph in paragraphs) {
            val lines = wrapText(paragraph, bodyPaint, textWidth)
            for (line in lines) {
                if (yPos + lineHeight > pageHeight - margin - 20f) {
                    // Draw footer
                    canvas.drawText("Page $pageNumber — MEYOU Reader Export", margin, pageHeight - margin, footerPaint)
                    document.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = margin + 20f
                }
                canvas.drawText(line, margin, yPos, bodyPaint)
                yPos += lineHeight
            }
            yPos += 8f // paragraph gap
        }

        canvas.drawText("Page $pageNumber — MEYOU Reader Export", margin, pageHeight - margin, footerPaint)
        document.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
    }

    private fun generateArticlePdf(article: Article, outputFile: File) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 48f

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paragraphs = article.content.split(Regex("(\r?\n)+")).filter { it.isNotBlank() }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var yPos = margin + 20f

        canvas.drawText(article.title, margin, yPos, titlePaint)
        yPos += 22f
        canvas.drawText("${article.author} · ${article.source} · ${article.readTimeMinutes} min read", margin, yPos, metaPaint)
        yPos += 26f

        val textWidth = pageWidth - 2 * margin
        val lineHeight = 16f

        for (paragraph in paragraphs) {
            val lines = wrapText(paragraph, bodyPaint, textWidth)
            for (line in lines) {
                if (yPos + lineHeight > pageHeight - margin - 20f) {
                    canvas.drawText("Page $pageNumber — MEYOU Reader Export", margin, pageHeight - margin, footerPaint)
                    document.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = margin + 20f
                }
                canvas.drawText(line, margin, yPos, bodyPaint)
                yPos += lineHeight
            }
            yPos += 8f
        }

        canvas.drawText("Page $pageNumber — MEYOU Reader Export", margin, pageHeight - margin, footerPaint)
        document.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth) {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    lines.add(word)
                    currentLine = StringBuilder()
                }
            } else {
                currentLine = StringBuilder(testLine)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    // -------------------------------------------------------------
    // TXT Generation
    // -------------------------------------------------------------
    private fun generateBookTxt(book: Book, outputFile: File) {
        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("${book.title.uppercase()}\n")
        sb.append("Author: ${book.author}\n")
        sb.append("Category: ${book.category}\n")
        sb.append("Reading Progress: ${(book.progressPercent * 100).toInt()}%\n")
        sb.append("Exported from: MEYOU Reader\n")
        sb.append("=========================================\n\n")
        sb.append(book.content)
        sb.append("\n\n---\nExport Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
        outputFile.writeText(sb.toString(), Charsets.UTF_8)
    }

    private fun generateArticleTxt(article: Article, outputFile: File) {
        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("${article.title.uppercase()}\n")
        sb.append("Author: ${article.author} | Source: ${article.source}\n")
        sb.append("Category: ${article.category} | Read Time: ${article.readTimeMinutes} min\n")
        sb.append("Exported from: MEYOU Reader\n")
        sb.append("=========================================\n\n")
        sb.append(article.content)
        sb.append("\n\n---\nExport Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
        outputFile.writeText(sb.toString(), Charsets.UTF_8)
    }

    // -------------------------------------------------------------
    // CSV Generation
    // -------------------------------------------------------------
    private fun generateBookCsv(book: Book, outputFile: File) {
        val sb = StringBuilder()
        sb.append("Type,ID,Title,Author,Category,TotalPages,CurrentPage,ProgressPercent,Synopsis,Content\n")
        sb.append(
            listOf(
                "BOOK",
                escapeCsv(book.id),
                escapeCsv(book.title),
                escapeCsv(book.author),
                escapeCsv(book.category),
                book.totalPages.toString(),
                book.currentPage.toString(),
                book.progressPercent.toString(),
                escapeCsv(book.synopsis),
                escapeCsv(book.content)
            ).joinToString(",")
        )
        outputFile.writeText(sb.toString(), Charsets.UTF_8)
    }

    private fun generateArticleCsv(article: Article, outputFile: File) {
        val sb = StringBuilder()
        sb.append("Type,ID,Title,Author,Source,Category,ReadTimeMinutes,PublishDate,Summary,Content\n")
        sb.append(
            listOf(
                "ARTICLE",
                escapeCsv(article.id),
                escapeCsv(article.title),
                escapeCsv(article.author),
                escapeCsv(article.source),
                escapeCsv(article.category),
                article.readTimeMinutes.toString(),
                escapeCsv(article.publishDate),
                escapeCsv(article.summary),
                escapeCsv(article.content)
            ).joinToString(",")
        )
        outputFile.writeText(sb.toString(), Charsets.UTF_8)
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    // -------------------------------------------------------------
    // JSON Generation
    // -------------------------------------------------------------
    private fun generateBookJson(book: Book, outputFile: File) {
        val json = JSONObject().apply {
            put("type", "BOOK")
            put("id", book.id)
            put("title", book.title)
            put("author", book.author)
            put("category", book.category)
            put("totalPages", book.totalPages)
            put("currentPage", book.currentPage)
            put("progressPercent", book.progressPercent)
            put("currentChapter", book.currentChapter)
            put("synopsis", book.synopsis)
            put("accentColorHex", book.accentColorHex)
            put("isBookmarked", book.isBookmarked)
            put("customCoverUri", book.customCoverUri)
            put("content", book.content)
            put("exportedAt", System.currentTimeMillis())
            put("app", "MEYOU Reader")
        }
        outputFile.writeText(json.toString(2), Charsets.UTF_8)
    }

    private fun generateArticleJson(article: Article, outputFile: File) {
        val json = JSONObject().apply {
            put("type", "ARTICLE")
            put("id", article.id)
            put("title", article.title)
            put("author", article.author)
            put("source", article.source)
            put("category", article.category)
            put("readTimeMinutes", article.readTimeMinutes)
            put("publishDate", article.publishDate)
            put("summary", article.summary)
            put("featuredQuote", article.featuredQuote)
            put("accentColorHex", article.accentColorHex)
            put("isBookmarked", article.isBookmarked)
            put("customCoverUri", article.customCoverUri)
            put("content", article.content)
            put("progressPercent", article.progressPercent)
            put("exportedAt", System.currentTimeMillis())
            put("app", "MEYOU Reader")
        }
        outputFile.writeText(json.toString(2), Charsets.UTF_8)
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(40)
    }
}
