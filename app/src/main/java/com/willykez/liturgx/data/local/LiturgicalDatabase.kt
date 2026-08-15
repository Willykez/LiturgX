package com.willykez.liturgx.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedReadingEntity::class,
        BookmarkEntity::class,
        UserPreferencesEntity::class,
        LectionaryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class LiturgicalDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun lectionaryDao(): LectionaryDao

    companion object {
        @Volatile
        private var INSTANCE: LiturgicalDatabase? = null

        fun getDatabase(context: Context): LiturgicalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LiturgicalDatabase::class.java,
                    "liturgical_calendar.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
