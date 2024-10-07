package com.example.drawingapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "drawings")
@TypeConverters(Converters::class)
data class Drawing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val serializedPaths: String,
    val thumbnail: String = ""
)

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

data class SerializablePath(
    val points: List<Float>,
    val color: Int,
    val strokeWidth: Float,
    val alpha: Int,
    val shape: PenShape
)
