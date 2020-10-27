package com.example.samsa.room.helper

import com.example.samsa.room.dao.SearchHistoryDao
import com.example.samsa.room.entity.SearchHistoryEntity

class SearchHistoryRepo(private val historyDao: SearchHistoryDao) {
    val getHistory = historyDao.getHistory()

    suspend fun insertTags(tags: String) {
        historyDao.insertTags(SearchHistoryEntity(tags, System.currentTimeMillis()))
    }

    suspend fun deleteHistory(tags: String) {
        historyDao.deleteHistory(tags)
    }
}