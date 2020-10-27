package com.example.samsa.tools.glide

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.igreenwood.loupe.Loupe

class GlideLoupeImageLoader(
    val mImageView: ImageView,
    val mContainer: ViewGroup,
    val mProgressBar: ProgressBar,
    val mErrorTextView: TextView,
    val loupeInitializer: (ImageView, ViewGroup) -> Loupe
) {
    private var loupe: Loupe? = null
    var imgLoaded = false
        private set

    fun resetImagePosition() {
        loupe?.restoreViewTransform()
    }

    fun cleanup() {
        loupe?.cleanup()
    }

    fun load(url: String) {
        var indeterminateflag = true
        ProgressAppGlideModule.expect(
            url,
            object : ProgressAppGlideModule.UIonProgressListener {
                override fun onProgress(bytesRead: Long, expectedLength: Long) {
                    mProgressBar.apply {
                        if (indeterminateflag) {
                            isIndeterminate = false
                            indeterminateflag = false
                        }
                        max = 100
                        progress = (100 * bytesRead / expectedLength).toInt()
                    }
                }

                override fun getGranularityPercentage() = 1.0f
            })

        GlideApp.with(mImageView.context)
            .load(url)
            .timeout(10000)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    ProgressAppGlideModule.forget(url)
                    onFinished()
                    mErrorTextView.text = e?.message ?: "로딩중 오류"
                    mErrorTextView.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    ProgressAppGlideModule.forget(url)
                    loupe = loupeInitializer(mImageView, mContainer)
                    onFinished()
                    return false
                }
            }).into(mImageView)

    }

    private fun onFinished() {
        mProgressBar.visibility = View.GONE
        imgLoaded = true
    }
}