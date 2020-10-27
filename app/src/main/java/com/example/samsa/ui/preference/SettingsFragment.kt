package com.example.samsa.ui.preference

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.bumptech.glide.Glide
import com.example.samsa.BuildConfig
import com.example.samsa.R
import com.example.samsa.app.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SettingsFragment : PreferenceFragmentCompat(),
    TagDialog.TagDialogListener,
    NumberPickerDialog.NumberPickerDialogListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("version_info")?.let {
            it.summary = BuildConfig.VERSION_NAME
        }

        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_key_dark_mode))?.let {
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, v ->
                if (v as Boolean)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                return@OnPreferenceChangeListener true
            }
        }

        findPreference<Preference>(getString(R.string.pref_key_blacklist))?.let {
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val blacklist = preferenceManager.sharedPreferences.getStringSet(
                    getString(R.string.pref_key_blacklist),
                    setOf()
                )!!
                TagDialog(this, blacklist.toMutableSet()).show()

                return@OnPreferenceClickListener true
            }
        }

        findPreference<Preference>(getString(R.string.pref_key_preload_count))?.let {
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val selected = preferenceManager.sharedPreferences.getInt(
                    getString(R.string.pref_key_preload_count),
                    1
                )
                NumberPickerDialog(this, selected).show()
                return@OnPreferenceClickListener true
            }
        }

        findPreference<Preference>(getString(R.string.pref_key_display_tutorial))?.let {
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                with(preferenceManager.sharedPreferences.edit()) {
                    putBoolean(getString(R.string.pref_key_display_tutorial), true)
                    apply()
                }
                showTextSaved()
                return@OnPreferenceClickListener true
            }
        }

        findPreference<Preference>("delete_glide_cache")?.let {
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setMessage(getString(R.string.pref_confirm_delete_image_cache))
                    setPositiveButton(R.string.ok) { _, _ ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            val pd = ProgressDialog(context, "Deleting cache")
                            pd.show()
                            withContext(Dispatchers.IO) {
                                Glide.get(context).clearDiskCache(); delay(1000L)
                            }
                            pd.dismiss()
                        }
                    }
                    setNeutralButton(R.string.cancel, null)
                }.show()
                return@OnPreferenceClickListener true
            }
        }

        findPreference<Preference>("github_link")?.let {
            it.icon =
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_github_dark,
                        null
                    )
                } else {
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_github_light,
                        null
                    )
                }
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/klx7007/Samsa"))
                startActivity(browserIntent)
                return@OnPreferenceClickListener true
            }
        }

    }

    override fun onTagDialogPositiveClick(tags: MutableSet<String>) {
        val editor = preferenceManager.sharedPreferences.edit()
        editor.putStringSet(getString(R.string.pref_key_blacklist), tags)
        editor.apply()
        showTextSaved()
    }

    override fun onNumberPickerDialogPositiveClick(currentNum: Int) {
        val editor = preferenceManager.sharedPreferences.edit()
        editor.putInt(getString(R.string.pref_key_preload_count), currentNum)
        editor.apply()
        showTextSaved()
    }

    override fun onPause() {
        MyApplication.getInstance().recachePref()
        super.onPause()
    }

    fun showTextSaved() =
        Toast.makeText(context, getString(R.string.saved), Toast.LENGTH_SHORT).show()
}