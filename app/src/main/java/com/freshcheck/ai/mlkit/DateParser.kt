package com.freshcheck.ai.mlkit

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Utility to parse dates from raw text using regex.
 */
object DateParser {
    // Common date formats: DD/MM/YYYY, DD-MM-YYYY, YYYY-MM-DD, etc.
    private val DATE_REGEX = Pattern.compile(
        "\\b(\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4})\\b|" + // 01/01/2024
        "\\b(\\d{4}[./-]\\d{1,2}[./-]\\d{1,2})\\b"      // 2024/01/01
    )

    private val DATE_FORMATS = listOf(
        "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy",
        "yyyy/MM/dd", "yyyy-MM-dd", "yyyy.MM.dd",
        "dd/MM/yy", "dd-MM-yy"
    )

    /**
     * Extracts a date from a string and returns it as a Long timestamp.
     */
    fun extractDate(text: String): Long? {
        val matcher = DATE_REGEX.matcher(text)
        if (matcher.find()) {
            val dateStr = matcher.group()
            return parseDateString(dateStr)
        }
        return null
    }

    private fun parseDateString(dateStr: String): Long? {
        for (format in DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.isLenient = false
                val date = sdf.parse(dateStr)
                if (date != null) return date.time
            } catch (e: Exception) {
                // Ignore and try next format
            }
        }
        return null
    }
}
