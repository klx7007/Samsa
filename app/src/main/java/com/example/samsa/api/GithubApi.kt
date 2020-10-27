package com.example.samsa.api

import com.example.samsa.api.model.GithubReleases
import retrofit2.http.GET
import retrofit2.http.Headers

// TODO : Create Github check update function
interface GithubApi {
    @Headers("Accept: application/vnd.github.v3+json")
    @GET("/repos/klx7007/Samsa/releases?per_page=1")
    suspend fun getReleases(): List<GithubReleases>
}