package com.example.samsa.ui.main.media

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.samsa.api.model.PostModel
import com.example.samsa.ui.main.PostRVAdapter
import com.example.samsa.ui.main.media.pages.ImagePageFragment
import com.example.samsa.ui.main.media.pages.VideoPageFragment

abstract class PostHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(post: PostModel)
}

class MediaPagerAdapter(fa: Fragment) : FragmentStateAdapter(fa), PostRVAdapter {

    override var posts = ArrayList<PostModel>()
        set(value) {
            posts.clear()
            posts.addAll(value)
        }

    override fun getItemCount() = posts.size

    override fun createFragment(position: Int): Fragment {
        val post = posts[position]
        return if (post.isVid)
            VideoPageFragment.newInstance(post.postId)
        else
            ImagePageFragment.newInstance(post.postId)
    }

}