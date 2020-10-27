package com.example.samsa.tools

open class Event<out T>(private val content: T?) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        synchronized(this) {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }
    }

    fun iHandledIt() {
        synchronized(this) {
            hasBeenHandled = true
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T? = content
}

open class TriggerEvent {
    var hasBeenHandled = false
        private set
        get() {
            hasBeenHandled = true
            return false
        }
}