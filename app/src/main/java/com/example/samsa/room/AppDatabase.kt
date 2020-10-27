package com.example.samsa.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.samsa.room.dao.SearchHistoryDao
import com.example.samsa.room.dao.XxxPostDao
import com.example.samsa.room.entity.SearchHistoryEntity
import com.example.samsa.room.entity.XxxPostEntity

@Database(
    entities = [XxxPostEntity::class, SearchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun xxxPostDao(): XxxPostDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDb(context: Context): AppDatabase {
            val tmpInstance = instance
            if (tmpInstance != null) return tmpInstance

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                this.instance = instance
                return instance
            }
        }
    }
}