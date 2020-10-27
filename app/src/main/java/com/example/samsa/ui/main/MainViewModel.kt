package com.example.samsa.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.example.samsa.R
import com.example.samsa.api.model.PostModel
import com.example.samsa.api.model.TagModel
import com.example.samsa.app.MyApplication
import com.example.samsa.room.AppDatabase
import com.example.samsa.room.helper.SearchHistoryRepo
import com.example.samsa.tools.Event
import com.example.samsa.tools.ListLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModelFactory(private val application: Application, private val tagName: String?) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        MainViewModel(application, tagName) as T
}

class MainViewModel(application: Application, intentTag: String?) : AndroidViewModel(application) {

    private val searchHistoryDao by lazy {
        AppDatabase.getDb(application).searchHistoryDao()
    }

    private val searchHistoryRepo by lazy {
        SearchHistoryRepo(searchHistoryDao)
    }

    private var currentTags = intentTag ?: ""
    var postListPosition = 0
    var position = 0
    var currentPage = 0
    var hasEnded = false

    // Shared Data
    private val _postMapCache: MutableLiveData<LinkedHashMap<String, PostModel>> by lazy {
        MutableLiveData<LinkedHashMap<String, PostModel>>().apply {
            value = linkedMapOf()
        }
    }

    val postMapCache: LiveData<LinkedHashMap<String, PostModel>>
        get() = _postMapCache

    // PostListFragment binding
    private val _listErrorText = MutableLiveData<Int>()
    private val _postList = ListLiveData<PostModel>()
    private val _tagList = ListLiveData<String>()
    private val _searchText = MutableLiveData<String>(intentTag ?: "")
    private val _isLoading = MutableLiveData<Boolean>(false)

    val listErrorText: LiveData<Int>
        get() = _listErrorText
    val postList: LiveData<ArrayList<PostModel>>
        get() = _postList
    val tagList: LiveData<ArrayList<String>>
        get() = _tagList
    val searchText: MutableLiveData<String>
        get() = _searchText
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    // Events
    private val _postClickEvent = MutableLiveData<Event<Any>>()
    private val _tagClickEvent = MutableLiveData<Event<TagModel>>()
    private val _mediaFragmentExitEvent = MutableLiveData<Event<Any>>()

    val postClickEvent: LiveData<Event<Any>>
        get() = _postClickEvent
    val tagClickEvent: LiveData<Event<TagModel>>
        get() = _tagClickEvent
    val mediaFragmentExitEvent: LiveData<Event<Any>>
        get() = _mediaFragmentExitEvent

    init {
        loadPosts()
    }

    /** PostListFragment **/

    fun onSearch() {
        currentTags = _searchText.value ?: ""
        _postList.clear(true)
        currentPage = 0
        hasEnded = false
        loadPosts()
    }

    fun onRefresh() {
        _postList.clear(true)
        currentPage = 0
        hasEnded = false
        loadPosts()
    }

    fun onPostClick(position: Int) {
        _postList.value?.let {
            this.position = position
            this._tagList.value = _postList.value?.get(position)?.tagsList
            // fire MediaFragment Event
            _postClickEvent.value = Event(null)
        }
    }

    fun loadPosts() {
        if (!hasEnded) {
            viewModelScope.launch {
                // Reset error text res
                _listErrorText.value = 0
                _isLoading.value = true

                val app = MyApplication.getInstance()
                val gal = app.getGallery()
                val blacklist = app.getPref().getStringSet("blacklistTags", setOf())

                // get PostList
                var finalSearchQuery = currentTags.trim()
                if (finalSearchQuery.isNotBlank()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        searchHistoryRepo.insertTags(finalSearchQuery)
                    }
                }

                blacklist?.forEach {
                    finalSearchQuery += " -$it"
                }

                val posts = try {
                    gal.getGalleryDoc(finalSearchQuery, currentPage)
                } catch (e: Exception) {
                    _listErrorText.value = R.string.list_load_error
                    return@launch
                } finally {
                    _isLoading.value = false
                }

                if (posts == null || posts.count() <= 0) {
                    _listErrorText.value = R.string.list_load_no_result
                    hasEnded = true
                } else {
                    if (posts.count() < 42) {
                        hasEnded = true
                    } else {
                        currentPage += 1
                    }
                    _postList.value?.let {
                        for (post in posts) {
                            _postMapCache.value?.put(post.postId, post)
                            if (!it.any { o -> o.postId == post.postId }) {
                                _postList.add(post)
                            }
                        }
                    }
                    _isLoading.value = false
                }

            }
        }
    }

    /** MediaFragment **/

    // when Viewpager page changed
    fun onPagerPageChange(position: Int) {
        if (position != 0) {
            this.position = position
            this._tagList.value = this._postList.value?.get(position)?.tagsList
            if (position > _postList.size() - 3 && _isLoading.value == false)
                loadPosts()
        }
    }

    // When tag is clicked on MediaFragment
    fun onTagClick(tagModel: TagModel) {
        _tagClickEvent.value = Event(tagModel)
    }

    fun onMediaFragmentExit() {
        postListPosition = position
        _mediaFragmentExitEvent.value = Event(null)
    }
}