package com.example.pet_project_frontend.data.local.database.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateConverters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(dateFormatter)
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = 
        dateString?.let { LocalDate.parse(it, dateFormatter) }
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.format(dateTimeFormatter)
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? = 
        dateTimeString?.let { LocalDateTime.parse(it, dateTimeFormatter) }
}

class ListConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? = gson.toJson(value)
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }
}