package com.example.samsa.api.model

data class TagModel(
    var tagName: String,
    var tagCount: Int? = null,
    var isHistory: Boolean = false
)