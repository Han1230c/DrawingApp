package com.example.drawingapp.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromString(value: String): List<SerializablePath> {
        val listType = object : TypeToken<List<SerializablePath>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<SerializablePath>): String {
        return Gson().toJson(list)
    }
}
