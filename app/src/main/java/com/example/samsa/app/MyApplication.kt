package com.example.samsa.app

import android.app.Application
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.samsa.R
import com.example.samsa.api.Gallery
import com.example.samsa.api.Rule34XXX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {
    private lateinit var pref: SharedPreferences

    companion object {
        private lateinit var appInstance: MyApplication

        fun getInstance(): MyApplication {
            return if (Companion::appInstance.isInitialized) {
                appInstance
            } else {
                throw Exception("getMyApplication Instance")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        appInstance = this

        if (pref.getBoolean(getString(R.string.pref_key_dark_mode), false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun getGallery(): Gallery {

        return when (pref.getString(getString(R.string.pref_key_gallery_list), "")) {
            //getString(R.string.paheal_url) -> Paheal
            getString(R.string.rule34_xxx_url) -> Rule34XXX
            //getString(R.string.rastafarian_url) -> Rastafarian
            else -> Rule34XXX
        }
    }

    fun recachePref() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
    }

    fun addTagToPreferenceSet(tag: String, @StringRes prefKeyId: Int) {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        val s = pref.getStringSet(getString(prefKeyId), mutableSetOf())!!.toMutableSet()
        s.add(tag)
        val editor = pref.edit()
        editor.putStringSet(getString(prefKeyId), s)
        editor.apply()
    }

    fun removeTagFromPreferenceSet(tag: String, @StringRes prefKeyId: Int) {
        GlobalScope.launch(Dispatchers.Default) {
            pref = PreferenceManager.getDefaultSharedPreferences(this@MyApplication)
            val s = pref.getStringSet(getString(prefKeyId), mutableSetOf())!!.toMutableSet()
            s.remove(tag)
            val editor = pref.edit()
            editor.putStringSet(getString(prefKeyId), s)
            editor.apply()
        }
    }

    fun getPref(): SharedPreferences {
        return pref
    }
}