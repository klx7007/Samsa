package com.example.samsa.tools

import android.content.Context
import android.content.res.Resources
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.samsa.api.model.PostModel
import com.example.samsa.ui.main.PostRVAdapter
import com.example.samsa.ui.main.media.MediaTagListAdapter
import com.google.android.material.card.MaterialCardView

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("onSearch")
    fun ClearableAutoCompleteTextView.setSearchListener(onSearch: () -> Unit) {
        setOnEditorActionListener { view, actionId, event ->
            val imeAction = when (actionId) {
                EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_SEND,
                EditorInfo.IME_ACTION_GO -> true
                else -> false
            }

            val keydownEvent = event?.keyCode == KeyEvent.KEYCODE_ENTER
                    && event.action == KeyEvent.ACTION_DOWN

            if (imeAction or keydownEvent)
                true.also {
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(windowToken, 0)
                    view.clearFocus()
                    onSearch()
                }
            else false
        }
    }

    @JvmStatic
    @BindingAdapter("posts")
    fun setRVPost(view: RecyclerView, posts: LiveData<ArrayList<PostModel>>) {
        view.adapter?.run {
            if (this is PostRVAdapter) {
                posts.value?.let { this.posts = it } ?: { this.posts = arrayListOf() }()
                this.notifyDataSetChanged()
            }
        }
    }

    @JvmStatic
    @BindingAdapter("posts")
    fun setVPPost(view: ViewPager2, posts: LiveData<ArrayList<PostModel>>) {
        view.adapter?.run {
            if (this is PostRVAdapter) {
                posts.value?.let { this.posts = it } ?: { this.posts = arrayListOf() }()
                this.notifyDataSetChanged()
            }
        }
    }

    @JvmStatic
    @BindingAdapter("tags")
    fun setBindTag(view: RecyclerView, tags: LiveData<ArrayList<String>>) {
        view.adapter?.run {
            if (this is MediaTagListAdapter) {
                tags.value?.let { this.tags = it } ?: { this.tags = arrayListOf() }()
                this.notifyDataSetChanged()
            }
        }
    }

    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(imageView: ImageView, url: String) {
        Glide.with(imageView.context)
            .load(url)
            //.apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imageView)
    }

    @JvmStatic
    @BindingAdapter("vidBorder")
    fun drawVidBorder(cardView: MaterialCardView, isVid: Boolean) {
        if (isVid) {
            cardView.strokeWidth = (2 * Resources.getSystem().displayMetrics.density).toInt()
        } else {
            cardView.strokeWidth = 0
        }
    }

    @JvmStatic
    @BindingAdapter("stringRes")
    fun loadResText(textView: TextView, stringRes: Int) {
        if (stringRes == 0) return
        textView.setText(stringRes)
    }
}

