package com.example.samsa.ui.preference

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.samsa.R
import com.example.samsa.tools.adapters.TagListAdapter

class TagDialog(private val frag: Fragment, private val tags: MutableSet<String>) :
    GenericDialog(frag.requireContext()), TagListAdapter.TagListListener {
    internal lateinit var listener: TagDialogListener

    private val adapterList = tags.toMutableList()

    interface TagDialogListener {
        fun onTagDialogNegativeClick() {}
        fun onTagDialogPositiveClick(tags: MutableSet<String>) {}
    }

    override fun onDialogPositiveClick() = listener.onTagDialogPositiveClick(tags)
    override fun onDialogNegativeClick() = listener.onTagDialogNegativeClick()

    override fun getDialogContentView(container: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.dialog_blacklist, container, false)

        val tagListView = view.findViewById<ListView>(R.id.tag_list) ?: throw Exception()
        val tagEditText = view.findViewById<EditText>(R.id.tag_editText) ?: throw Exception()
        val addTagButton = view.findViewById<Button>(R.id.add_tags_button) ?: throw Exception()

        tagListView.adapter = TagListAdapter(this, context, adapterList)

        addTagButton.setOnClickListener {
            val tagText = tagEditText.text
            if (!tagText.isNullOrBlank()) {
                tagText.split(" ", "\n").forEach {
                    if (it.isBlank()) return@forEach
                    tags.add(it)
                    adapterList.add(it)
                    (tagListView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                    tagEditText.text.clear()
                }
            }
        }

        return view
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = frag as TagDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (frag.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }

    override fun onTagClick(tag: String) {
        tags.remove(tag)
        adapterList.remove(tag)
    }
}