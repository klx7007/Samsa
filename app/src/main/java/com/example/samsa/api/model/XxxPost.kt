package com.example.samsa.api.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "posts")
class XxxPosts(
    @field:Attribute(name = "count")
    @param:Attribute(name = "count")
    var count: Int,
    @field:Attribute(name = "offset")
    @param:Attribute(name = "offset")
    var offset: Int,
    @field:ElementList(inline = true, name = "post", required = false)
    @param:ElementList(inline = true, name = "post", required = false)
    var postList: ArrayList<XxxPost>?
)

@Root(name = "post")
class XxxPost(
    @field:Attribute(name = "height")
    @param:Attribute(name = "height")
    var height: String,
    @field:Attribute(name = "score")
    @param:Attribute(name = "score")
    var score: String,
    @field:Attribute(name = "file_url")
    @param:Attribute(name = "file_url")
    var file_url: String,
    @field:Attribute(name = "parent_id")
    @param:Attribute(name = "parent_id")
    var parent_id: String,
    @field:Attribute(name = "sample_url")
    @param:Attribute(name = "sample_url")
    var sample_url: String,
    @field:Attribute(name = "sample_width")
    @param:Attribute(name = "sample_width")
    var sample_width: String,
    @field:Attribute(name = "sample_height")
    @param:Attribute(name = "sample_height")
    var sample_height: String,
    @field:Attribute(name = "preview_url")
    @param:Attribute(name = "preview_url")
    var preview_url: String,
    @field:Attribute(name = "rating")
    @param:Attribute(name = "rating")
    var rating: String,
    @field:Attribute(name = "tags")
    @param:Attribute(name = "tags")
    var tags: String,
    @field:Attribute(name = "id")
    @param:Attribute(name = "id")
    var id: String,
    @field:Attribute(name = "width")
    @param:Attribute(name = "width")
    var width: String,
    @field:Attribute(name = "change")
    @param:Attribute(name = "change")
    var change: String,
    @field:Attribute(name = "md5")
    @param:Attribute(name = "md5")
    var md5: String,
    @field:Attribute(name = "creator_id")
    @param:Attribute(name = "creator_id")
    var creator_id: String,
    @field:Attribute(name = "has_children")
    @param:Attribute(name = "has_children")
    var has_children: String,
    @field:Attribute(name = "created_at")
    @param:Attribute(name = "created_at")
    var created_at: String,
    @field:Attribute(name = "status")
    @param:Attribute(name = "status")
    var status: String,
    @field:Attribute(name = "source")
    @param:Attribute(name = "source")
    var source: String,
    @field:Attribute(name = "has_notes")
    @param:Attribute(name = "has_notes")
    var has_notes: String,
    @field:Attribute(name = "has_comments")
    @param:Attribute(name = "has_comments")
    var has_comments: String,
    @field:Attribute(name = "preview_width")
    @param:Attribute(name = "preview_width")
    var preview_width: String,
    @field:Attribute(name = "preview_height")
    @param:Attribute(name = "preview_height")
    var preview_height: String
) : PostModel {
    private val splitTags = ArrayList(tags.trim().split("\\s+".toRegex()))

    override val postId = id
    override val postThumbnailUrl = preview_url
    override val isVid: Boolean = file_url.endsWith(".webm")
    override val tagsList: ArrayList<String> = splitTags
    override val fileUrl: String? = file_url
    override val hasComments: Boolean? = has_comments.toBoolean()
    override val createdAt: String? = created_at
}
