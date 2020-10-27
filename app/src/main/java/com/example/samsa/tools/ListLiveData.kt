package com.example.samsa.tools

import androidx.lifecycle.MutableLiveData

// Custom LiveData List
class ListLiveData<T> : MutableLiveData<ArrayList<T>>() {
    init {
        value = arrayListOf()
    }

    fun size(): Int {
        return value?.size ?: 0
    }

    fun add(item: T) {
        value?.let {
            it.add(item)
            value = it
        }
    }

    fun addAll(list: ArrayList<T>) {
        value?.let {
            it.addAll(list)
            value = it
        }
    }

    fun clear(notify: Boolean) {
        value?.let {
            it.clear()
            if (notify) value = it
        }
    }

    fun notifyChange() {
        value?.let { value = it }
    }

    fun set(items: ArrayList<T>) {
        value?.let {
            it.clear()
            it.addAll(items)
            value = it
        }
    }
}