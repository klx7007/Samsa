package com.example.samsa.api.model

import com.google.gson.annotations.SerializedName

class GithubReleases(
    @SerializedName("tag_name")
    var tagName: String,
    @SerializedName("name")
    var name: String,
    @SerializedName("body")
    var body: String,
    @SerializedName("published_at")
    var publishedAt: String,
    @SerializedName("assets")
    var assets: List<GithubAssets>

)

class GithubAssets(
    @SerializedName("browser_download_url")
    var browserDownloadUrl: String,
    @SerializedName("content_type")
    var contentType: String,
)