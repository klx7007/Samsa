package com.example.samsa.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.samsa.room.entity.XxxPostEntity

@Dao
interface XxxPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg posts: XxxPostEntity)

    @Query("SELECT * FROM xxx_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: String): XxxPostEntity
}