package com.example.samsa.tools.glide

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.*
import okio.*
import java.io.InputStream
import java.util.*

@GlideModule
class ProgressAppGlideModule : AppGlideModule() {

    companion object {
        fun forget(url: String) {
            DispatchingProgressListener.forget(
                url
            )
        }

        fun expect(url: String, listener: UIonProgressListener) {
            DispatchingProgressListener.expect(
                url,
                listener
            )
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request()
                    val response = chain.proceed(request)

                    val listener =
                        DispatchingProgressListener()

                    return response.newBuilder()
                        .body(
                            OkHttpProgressResponseBody(
                                request.url,
                                response.body!!,
                                listener
                            )
                        )
                        .build()
                }
            })
            .build()

        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(client)
        )
    }

    private interface ResponseProgressListener {
        fun update(url: HttpUrl, bytesRead: Long, contentLength: Long)
    }

    interface UIonProgressListener {
        fun onProgress(bytesRead: Long, expectedLength: Long)
        fun getGranularityPercentage(): Float
    }

    private class DispatchingProgressListener :
        ResponseProgressListener {

        companion object {

            private val LISTENERS = WeakHashMap<String, UIonProgressListener>()
            private val PROGRESSES = WeakHashMap<String, Long>()

            fun forget(url: String) {
                LISTENERS.remove(url)
                PROGRESSES.remove(url)
            }

            fun expect(url: String, listener: UIonProgressListener) {
                LISTENERS[url] = listener
            }
        }

        private val handler = Handler(Looper.getMainLooper())

        override fun update(url: HttpUrl, bytesRead: Long, contentLength: Long) {

            val key = url.toString()
            val listener = LISTENERS[key] ?: return

            if (contentLength <= bytesRead) {
                forget(
                    key
                )
            }

            if (needsDispatch(key, bytesRead, contentLength, listener.getGranularityPercentage())) {
                handler.post {
                    listener.onProgress(bytesRead, contentLength)
                }
            }
        }

        private fun needsDispatch(
            key: String,
            current: Long,
            total: Long,
            granularity: Float
        ): Boolean {

            if (granularity == 0f || current == 0L || total == current) {
                return true
            }

            val percent = 100f * current / total
            val currentProgress = (percent / granularity).toLong()
            val lastProgress = PROGRESSES[key]

            return if (lastProgress == null || currentProgress != lastProgress) {
                PROGRESSES[key] = currentProgress

                true
            } else {
                false
            }
        }
    }

    private class OkHttpProgressResponseBody(
        private val url: HttpUrl,
        private val responseBody: ResponseBody,
        private val progressListener: ResponseProgressListener
    ) : ResponseBody() {

        private var bufferedSource: BufferedSource? = null

        override fun contentType(): MediaType? {
            return responseBody.contentType()
        }

        override fun contentLength(): Long {
            return responseBody.contentLength()
        }

        override fun source(): BufferedSource {
            if (bufferedSource == null) {
                bufferedSource = source(responseBody.source()).buffer()
            }

            return bufferedSource!!
        }

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead = 0L

                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    val fullLength = responseBody.contentLength()

                    if (bytesRead == -1L) {
                        totalBytesRead = fullLength
                    } else {
                        totalBytesRead += bytesRead
                    }

                    progressListener.update(url, totalBytesRead, fullLength)
                    return bytesRead
                }
            }
        }
    }
}