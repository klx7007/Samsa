package com.example.samsa.ui.main.media

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.samsa.BR
import com.example.samsa.R
import com.example.samsa.api.model.TagModel
import com.example.samsa.app.MyApplication
import com.example.samsa.databinding.TagItemBinding
import com.example.samsa.ui.main.MainViewModel

class MediaTagListAdapter(val vm: MainViewModel) :
    RecyclerView.Adapter<MediaTagListAdapter.TagHolder>() {
    var tags = ArrayList<String>()
        set(value) {
            tags.clear()
            tags.addAll(value)
        }

    inner class TagHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: TagItemBinding? = DataBindingUtil.bind(itemView)

        fun bind(item: String) {
            binding?.setVariable(BR.tagName, item)

            // post item click listener
            itemView.setOnClickListener {
                vm.onTagClick(TagModel(item))
            }

            itemView.setOnLongClickListener {
                AlertDialog.Builder(it.context).apply {
                    setMessage(it.context.getString(R.string.gallery_tag_dialog, item))
                    setNegativeButton(R.string.ok_blacklist)
                    { _, _ ->
                        MyApplication.getInstance()
                            .addTagToPreferenceSet(item, R.string.pref_key_blacklist)
                        Toast.makeText(
                            it.context,
                            it.context.getString(R.string.gallery_tag_blacked, item),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    setPositiveButton(R.string.ok_favorite) { _, _ ->
                        MyApplication.getInstance()
                            .addTagToPreferenceSet(item, R.string.pref_key_favorite)
                        Toast.makeText(
                            it.context,
                            it.context.getString(R.string.gallery_tag_favored, item),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    setNeutralButton(R.string.cancel, null)
                }.create().show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TagHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.tag_item,
            parent,
            false
        )
    )

    override fun getItemCount() = tags.size

    override fun onBindViewHolder(holder: TagHolder, position: Int) {
        holder.bind(tags[position])
    }
}