package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "meyou_preferences")

enum class ReadingFont(val label: String) {
    SANS("Sans"),
    SERIF("Serif"),
    SYSTEM("System")
}

enum class ReadingPageTheme(val label: String) {
    WARM("Warm"),
    CREAM("Cream"),
    LAVENDER("Lavender"),
    DARK("Dark")
}

enum class ReadingWidth(val label: String) {
    STANDARD("Standard"),
    COMPACT("Compact"),
    WIDE("Wide")
}

data class ReadingPreferences(
    val fontSizeSp: Float = 18f,
    val font: ReadingFont = ReadingFont.SERIF,
    val lineSpacingMultiplier: Float = 1.6f,
    val pageTheme: ReadingPageTheme = ReadingPageTheme.CREAM,
    val brightness: Float = 0.85f,
    val readingWidth: ReadingWidth = ReadingWidth.STANDARD,
    val isOnboardingComplete: Boolean = false,
    val isLibraryGrid: Boolean = true,
    val isDarkMode: Boolean? = null // null means system
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val FONT_SIZE = floatPreferencesKey("reading_font_size")
        val FONT_FAMILY = stringPreferencesKey("reading_font_family")
        val LINE_SPACING = floatPreferencesKey("reading_line_spacing")
        val PAGE_THEME = stringPreferencesKey("reading_page_theme")
        val BRIGHTNESS = floatPreferencesKey("reading_brightness")
        val READING_WIDTH = stringPreferencesKey("reading_width")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val LIBRARY_GRID = booleanPreferencesKey("library_grid")
        val DARK_MODE = stringPreferencesKey("dark_mode_preference")
    }

    val preferencesFlow: Flow<ReadingPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 18f
            val fontName = preferences[PreferencesKeys.FONT_FAMILY] ?: ReadingFont.SERIF.name
            val font = try { ReadingFont.valueOf(fontName) } catch (e: Exception) { ReadingFont.SERIF }
            val lineSpacing = preferences[PreferencesKeys.LINE_SPACING] ?: 1.6f
            val themeName = preferences[PreferencesKeys.PAGE_THEME] ?: ReadingPageTheme.CREAM.name
            val pageTheme = try { ReadingPageTheme.valueOf(themeName) } catch (e: Exception) { ReadingPageTheme.CREAM }
            val brightness = preferences[PreferencesKeys.BRIGHTNESS] ?: 0.85f
            val widthName = preferences[PreferencesKeys.READING_WIDTH] ?: ReadingWidth.STANDARD.name
            val readingWidth = try { ReadingWidth.valueOf(widthName) } catch (e: Exception) { ReadingWidth.STANDARD }
            val onboarding = preferences[PreferencesKeys.ONBOARDING_COMPLETE] ?: false
            val isGrid = preferences[PreferencesKeys.LIBRARY_GRID] ?: true
            val darkModePref = preferences[PreferencesKeys.DARK_MODE]

            ReadingPreferences(
                fontSizeSp = fontSize,
                font = font,
                lineSpacingMultiplier = lineSpacing,
                pageTheme = pageTheme,
                brightness = brightness,
                readingWidth = readingWidth,
                isOnboardingComplete = onboarding,
                isLibraryGrid = isGrid,
                isDarkMode = when (darkModePref) {
                    "DARK" -> true
                    "LIGHT" -> false
                    else -> null
                }
            )
        }

    suspend fun updateFontSize(size: Float) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SIZE] = size }
    }

    suspend fun updateFont(font: ReadingFont) {
        context.dataStore.edit { it[PreferencesKeys.FONT_FAMILY] = font.name }
    }

    suspend fun updateLineSpacing(spacing: Float) {
        context.dataStore.edit { it[PreferencesKeys.LINE_SPACING] = spacing }
    }

    suspend fun updatePageTheme(theme: ReadingPageTheme) {
        context.dataStore.edit { it[PreferencesKeys.PAGE_THEME] = theme.name }
    }

    suspend fun updateBrightness(brightness: Float) {
        context.dataStore.edit { it[PreferencesKeys.BRIGHTNESS] = brightness }
    }

    suspend fun updateReadingWidth(width: ReadingWidth) {
        context.dataStore.edit { it[PreferencesKeys.READING_WIDTH] = width.name }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setLibraryGrid(isGrid: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.LIBRARY_GRID] = isGrid }
    }

    suspend fun setDarkMode(mode: Boolean?) {
        context.dataStore.edit {
            if (mode == null) {
                it.remove(PreferencesKeys.DARK_MODE)
            } else {
                it[PreferencesKeys.DARK_MODE] = if (mode) "DARK" else "LIGHT"
            }
        }
    }
}
