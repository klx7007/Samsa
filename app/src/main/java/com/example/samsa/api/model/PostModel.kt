package com.example.samsa.api.model

interface PostModel {
    val postId: String
    val postThumbnailUrl: String
    val isVid: Boolean
    val tagsList: ArrayList<String>
    val fileUrl: String?
    val hasComments: Boolean?
    val createdAt: String?
}