package com.example.drawingapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Drawing::class],
    version = 4,  // Incremented version number due to new field addition
    exportSchema = false
)
@TypeConverters(Converters::class)  // Ensure converters are included
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton instance for accessing the database
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "drawing_database"
                )
                    .fallbackToDestructiveMigration() // Ensures database is rebuilt on version upgrade
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
