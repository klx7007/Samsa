package com.example.samsa.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiHelper {
    fun createGithubService(): GithubApi {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(GithubApi::class.java)
    }
}