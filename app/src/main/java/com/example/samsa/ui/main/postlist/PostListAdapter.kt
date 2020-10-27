package com.example.samsa.ui.main.postlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.samsa.BR
import com.example.samsa.R
import com.example.samsa.api.model.PostModel
import com.example.samsa.databinding.PostItemBinding
import com.example.samsa.ui.main.MainViewModel
import com.example.samsa.ui.main.PostRVAdapter

class PostListAdapter(val vm: MainViewModel) : RecyclerView.Adapter<PostListAdapter.PostHolder>(),
    PostRVAdapter {
    private var RVPosition = 0
    override var posts = ArrayList<PostModel>()
        set(value) {
            posts.clear()
            posts.addAll(value)
        }

    class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: PostItemBinding? = DataBindingUtil.bind(itemView)

        fun bind(item: PostModel) {
            binding?.setVariable(BR.postItem, item)

            itemView.setOnLongClickListener {
                Toast.makeText(itemView.context, item.postId, Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PostHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.post_item,
            parent,
            false
        )
    )

    override fun getItemCount() = posts.size

    override fun getItemId(position: Int): Long = posts[position].postId.toLong()

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.bind(posts[position])

        holder.itemView.setOnClickListener { vm.onPostClick(position) }

        // Load more posts at the end
        if (posts.size - 1 == position) {
            vm.loadPosts()
        }
    }

    fun getPosition() = RVPosition

}