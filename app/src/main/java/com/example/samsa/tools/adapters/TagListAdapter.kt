package com.example.samsa.tools.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import com.example.samsa.R

class TagListAdapter(
    val listener: TagListListener,
    context: Context,
    list: MutableList<String> = arrayListOf()
) :
    ArrayAdapter<String>(context, R.layout.tag_item, list) {

    interface TagListListener {
        fun onTagClick(tag: String) {}
        fun onTagLongClick(tag: String) {}
    }

    fun changeList(list: ArrayList<String>) {
        clear()
        addAll(list)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.tag_item, parent, false)

        v.findViewById<Button>(R.id.tag_button)?.apply {
            text = getItem(position)
            setOnClickListener {
                getItem(position)?.let {
                    listener.onTagClick(it)
                }
                notifyDataSetChanged()
            }
            setOnLongClickListener {
                getItem(position)?.let {
                    listener.onTagLongClick(it)
                }
                notifyDataSetChanged()
                true
            }
        }
        return v
    }
}