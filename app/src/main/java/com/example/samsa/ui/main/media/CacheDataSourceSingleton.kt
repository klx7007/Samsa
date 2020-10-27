package com.example.samsa.ui.main.media

import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util

class CacheDataSourceSingleton {

    companion object {
        private lateinit var instance: DataSource.Factory

        fun getInstance(context: Context): DataSource.Factory {
            if (this::instance.isInitialized) {
                return instance
            } else {
                val simpleCache = SimpleCache(
                    context.cacheDir,
                    LeastRecentlyUsedCacheEvictor(10 * 1024 * 1024),
                    ExoDatabaseProvider(context)
                )
                val dataSourceFactory = DefaultDataSourceFactory(
                    context,
                    Util.getUserAgent(context, "SomePhone")
                )
                instance = CacheDataSourceFactory(simpleCache, dataSourceFactory)

                return instance
            }
        }

    }
}