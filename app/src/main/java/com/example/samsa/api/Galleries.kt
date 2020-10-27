package com.example.samsa.api

import androidx.core.text.HtmlCompat
import com.example.samsa.api.model.PostModel
import com.example.samsa.api.model.TagModel
import com.example.samsa.api.model.XxxPosts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.jsoup.Connection
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

interface Gallery {
    val baseUrl: String
    suspend fun getGalleryDoc(tags: String?, page: Int): ArrayList<PostModel>?
    suspend fun getImageUrl(postId: String): String
    suspend fun getVideoUrl(postId: String): String
    suspend fun getAutocomplete(query: String): List<TagModel>
}

object Rule34XXX : Gallery {

    override val baseUrl = "https://rule34.xxx/index.php?"
    private val pid = 42

    private val baseOkHttpClient: OkHttpClient by lazy {
        OkHttpClient()
    }

    private val retrofitGsonInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://rule34.xxx")
            .client(baseOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitSxmlInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://rule34.xxx")
            .client(baseOkHttpClient)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
    }

    @Serializable
    data class ImageModel(
        @SerialName("domain")
        val domain: String,
        @SerialName("width")
        val width: Int,
        @SerialName("height")
        val height: Int,
        @SerialName("dir")
        val dir: Int,
        @SerialName("img")
        val img: String,
        @SerialName("base_dir")
        val base_dir: String,
        @SerialName("sample_dir")
        val sample_dir: String,
        @SerialName("sample_width")
        val sample_width: String,
        @SerialName("sample_height")
        val sample_height: String
    )

    override suspend fun getAutocomplete(query: String): List<TagModel> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<TagModel>()

            var prefix = ""
            val searchQuery = if (query.startsWith("-")) {
                prefix += "-"
                query.removePrefix("-")
            } else {
                query
            }

            val xxxApi = retrofitGsonInstance.create(XxxApi::class.java)

            val res = try {
                xxxApi.getAutocomplete(searchQuery)
            } catch (e: Exception) {
                return@withContext result
            }

            val labelRgx = Regex("\\((\\d+)\\)\\s*$")
            for (i in res) {
                result.add(
                    TagModel(
                        prefix + HtmlCompat.fromHtml(
                            i.value,
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        ),
                        labelRgx.find(i.label)?.groupValues?.last()?.toInt() ?: 0
                    )
                )
            }

            return@withContext result
        }

    override suspend fun getGalleryDoc(tags: String?, page: Int): ArrayList<PostModel>? =
        withContext(Dispatchers.IO) {
            lateinit var resPosts: XxxPosts
            val xxxApi = retrofitSxmlInstance.create(XxxApi::class.java)
            resPosts = xxxApi.getPosts(tags, page, 42)

            return@withContext resPosts.postList as ArrayList<PostModel>?
        }

    override suspend fun getImageUrl(postId: String): String = withContext(Dispatchers.IO) {
        val res: Connection.Response

        try {
            res = Jsoup.connect("${baseUrl}page=post&s=view&id=$postId").execute()
        } catch (e: Throwable) {
            return@withContext ""
        }
        if (res.statusCode() != 200) {
            throw Exception("Bad response from server")
        }

        val regexResult =
            Regex("//<!\\[CDATA\\[[\\n\\t\\s]*image\\s*=\\s*(.*?);[\\n\\t\\s]*//]]>").find(res.body())
                ?: throw Exception("image data body parse error")
        val imgClass = Json.decodeFromString<ImageModel>(
            regexResult.groups[1]?.value?.replace("'", "\"")
                ?: throw Exception("image data json parse error")
        )

        return@withContext with(imgClass) { "$domain/$base_dir/$dir/$img" }
    }

    override suspend fun getVideoUrl(postId: String): String = withContext(Dispatchers.IO) {
        val res = Jsoup.connect("${baseUrl}page=post&s=view&id=$postId").execute()

        if (res.statusCode() != 200)
            throw Exception("video post res 200")

        val doc = res.parse()

        try {
            return@withContext doc.getElementById("gelcomVideoPlayer").children().last().attr("src")
        } catch (e: Exception) {
            throw Exception("XXX Video src parse error postId: $postId")
        }
    }
}
