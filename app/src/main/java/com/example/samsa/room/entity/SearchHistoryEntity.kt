package com.example.samsa.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "search_history", indices = [Index(value = ["tags"], unique = true)])
class SearchHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "tags") val tags: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
)