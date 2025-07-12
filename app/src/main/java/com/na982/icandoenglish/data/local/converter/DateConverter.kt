package com.na982.icandoenglish.data.local.converter

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Room Database에서 날짜 변환을 위한 컨버터
 */
class DateConverter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    @TypeConverter
    fun fromString(value: String?): Date? {
        return value?.let { dateFormat.parse(it) }
    }
    
    @TypeConverter
    fun dateToString(date: Date?): String? {
        return date?.let { dateFormat.format(it) }
    }
} 