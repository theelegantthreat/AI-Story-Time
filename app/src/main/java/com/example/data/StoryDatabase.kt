package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
  @Query("SELECT * FROM stories ORDER BY updatedAt DESC")
  fun getAllStories(): Flow<List<StoryEntity>>

  @Query("SELECT * FROM stories WHERE id = :id LIMIT 1")
  suspend fun getStoryById(id: String): StoryEntity?

  @Query("SELECT * FROM stories WHERE LOWER(TRIM(prompt)) = LOWER(TRIM(:prompt)) LIMIT 1")
  suspend fun findStoryByExactPrompt(prompt: String): StoryEntity?

  @Query("SELECT * FROM stories WHERE prompt LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' LIMIT 1")
  suspend fun findStoryByKeyword(query: String): StoryEntity?

  @Query("SELECT * FROM stories WHERE genreName = :genreName ORDER BY updatedAt DESC")
  fun getStoriesByGenre(genreName: String): Flow<List<StoryEntity>>

  @Query("SELECT COUNT(*) FROM stories")
  fun getStoryCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM stories")
  suspend fun getStoryCountDirect(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateStory(story: StoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(stories: List<StoryEntity>)

  @Query("DELETE FROM stories WHERE id = :id")
  suspend fun deleteStory(id: String)

  @Query("DELETE FROM stories")
  suspend fun clearAllStories()

  @Query("UPDATE stories SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun setFavorite(id: String, isFavorite: Boolean)
}

@Database(entities = [StoryEntity::class], version = 2, exportSchema = false)
abstract class StoryDatabase : RoomDatabase() {
  abstract fun storyDao(): StoryDao

  companion object {
    @Volatile
    private var INSTANCE: StoryDatabase? = null

    fun getDatabase(context: Context): StoryDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          StoryDatabase::class.java,
          "ai_story_time.db"
        )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
