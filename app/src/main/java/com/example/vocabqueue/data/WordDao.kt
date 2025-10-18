package com.example.vocabqueue.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM word_queue ORDER BY word ASC")
    fun getAll(): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: WordEntity)

    @Query("DELETE FROM word_queue WHERE word = :w")
    suspend fun delete(w: String)

    @Query("DELETE FROM word_queue")
    suspend fun clear()
}
