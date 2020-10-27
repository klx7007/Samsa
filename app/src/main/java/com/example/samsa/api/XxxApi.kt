package com.example.samsa.api

import com.example.samsa.api.model.XxxAutocomplete
import com.example.samsa.api.model.XxxPosts
import retrofit2.http.GET
import retrofit2.http.Query

interface XxxApi {
    @GET("/index.php?page=dapi&s=post&q=index")
    suspend fun getPosts(
        @Query("tags") tags: String? = null,
        @Query("pid") pid: Int = 0,
        @Query("limit") limit: Int = 42
    ): XxxPosts

    @GET("/autocomplete.php")
    suspend fun getAutocomplete(
        @Query("q") q: String
    ): List<XxxAutocomplete>
}