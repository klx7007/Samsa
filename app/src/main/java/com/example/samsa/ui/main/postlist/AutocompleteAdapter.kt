package com.example.samsa.ui.main.postlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.example.samsa.R
import com.example.samsa.api.model.TagModel
import com.example.samsa.app.MyApplication
import com.example.samsa.room.AppDatabase
import com.example.samsa.room.entity.SearchHistoryEntity
import com.example.samsa.room.helper.SearchHistoryRepo
import kotlinx.coroutines.*
import java.util.*

class AutocompleteAdapter(
    lifecycleOwner: LifecycleOwner,
    context: Context,
    private val layoutRes: Int
) :
    ArrayAdapter<TagModel>(context, layoutRes) {

    private val searchHistoryRepo by lazy {
        SearchHistoryRepo(AppDatabase.getDb(context).searchHistoryDao())
    }

    private val searchHistoryLiveData by lazy {
        searchHistoryRepo.getHistory
    }

    lateinit var searchHistoryList: List<SearchHistoryEntity>

    init {
        searchHistoryLiveData.observe(lifecycleOwner, { historyList ->
            searchHistoryList = historyList
        })
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(context)
            .inflate(layoutRes, parent, false)

        val tagTextView = v.findViewById<TextView>(R.id.autocomplete_text)
        val preIcon by lazy { v.findViewById<ImageView>(R.id.autocomplete_pre_icon) }
        val postIcon by lazy { v.findViewById<ImageView>(R.id.autocomplete_post_icon) }

        getItem(position)?.let {
            tagTextView.text = if (it.tagCount != null) {
                "${it.tagName} (${it.tagCount})"
            } else {
                it.tagName
            }

            if (it.isHistory) {
                preIcon.setImageResource(R.drawable.ic_baseline_history_24)
                preIcon.visibility = View.VISIBLE
                postIcon.apply {
                    setOnClickListener { _ ->
                        remove(it)
                        notifyDataSetChanged()
                        GlobalScope.launch(Dispatchers.IO) {
                            searchHistoryRepo.deleteHistory(it.tagName)
                        }
                    }
                    visibility = View.VISIBLE
                }
            } else {
                preIcon.setImageResource(R.drawable.ic_outline_local_offer_24)
                preIcon.visibility = View.VISIBLE
            }
        }

        return v
    }

    override fun getFilter() = filter

    private val filter = object : Filter() {
        private var preSearchText = ""
        private var currentJob: Job? = null

        override fun performFiltering(input: CharSequence?): FilterResults {
            currentJob?.cancel()
            val results = FilterResults()

            if (input.isNullOrBlank()) {
                preSearchText = ""
                searchHistoryList.map {
                    TagModel(it.tags, isHistory = true)
                }.let {
                    results.values = it
                    results.count = it.size
                }

                return results
            }

            val queryTag = input.trim().split(" ").last().toLowerCase(Locale.ROOT)
            if (queryTag.isBlank()) {
                currentJob?.cancel()
                return results
            }

            preSearchText = input.toString()
            val suggestions = arrayListOf<TagModel>()

            currentJob = GlobalScope.launch(Dispatchers.Main) {

                val tags = withContext(Dispatchers.Default) {
                    delay(350)
                    MyApplication.getInstance().getGallery().getAutocomplete(queryTag)
                }
                suggestions.addAll(tags)

                results.values = suggestions
                results.count = suggestions.size

                publishResults(input, results)
            }

            return results
        }

        override fun publishResults(p0: CharSequence?, results: FilterResults?) {
            clear()
            results?.values?.let {
                addAll(it as List<TagModel>)
            }
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            val preTags = preSearchText.trim().split(" ").dropLast(1).joinToString(" ").let {
                if (it.isNotEmpty()) "$it "
                else it
            }
            return "$preTags${(resultValue as TagModel).tagName} "
        }

    }
}