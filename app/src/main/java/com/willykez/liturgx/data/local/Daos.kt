package com.willykez.liturgx.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Query("SELECT * FROM cached_readings WHERE dateString = :date limit 1")
    suspend fun getReadingByDate(date: String): CachedReadingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: CachedReadingEntity)

    @Query("DELETE FROM cached_readings WHERE cachedAt < :threshold")
    suspend fun deleteOldCache(threshold: Long)

    /** Wipes every cached reading — used when a calendar-engine fix means old cached entries are wrong. */
    @Query("DELETE FROM cached_readings")
    suspend fun clearAll()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY savedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    fun isBookmarkedFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)
}

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getPreferencesFlow(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferences(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(prefs: UserPreferencesEntity)
}

@Dao
interface LectionaryDao {
    /**
     * cycle = 'ALL' matches rows that apply regardless of the A/B/C or I/II cycle
     * (e.g. most solemnities), so it's checked as a fallback alongside the requested cycle.
     */
    @Query(
        """
        SELECT * FROM lectionary_readings
        WHERE season = :season
        AND weekNumber = :weekNumber
        AND dayOfWeek = :dayOfWeek
        AND (cycle = :cycle OR cycle = 'ALL')
        ORDER BY (cycle = :cycle) DESC
        LIMIT 1
        """
    )
    suspend fun getReadingForDay(
        season: String,
        weekNumber: Int,
        dayOfWeek: String,
        cycle: String
    ): LectionaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LectionaryEntity>)

    @Query("SELECT COUNT(*) FROM lectionary_readings")
    suspend fun count(): Int

    /** Wipes the table — used when the bundled dataset version changes, so a stale/partial
     *  import from an earlier build doesn't block the current dataset from loading. */
    @Query("DELETE FROM lectionary_readings")
    suspend fun clearAll()
}
