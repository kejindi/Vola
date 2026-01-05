package com.vola.app.data.local.database.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalTime

class LocalTimeConverter {
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.toString()
    }
    
    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it) }
    }
}