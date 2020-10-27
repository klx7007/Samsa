package com.example.samsa.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.samsa.room.entity.SearchHistoryEntity

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(vararg searchHistoryEntity: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 50")
    fun getHistory(): LiveData<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE tags = :tags")
    suspend fun deleteHistory(tags: String)
}