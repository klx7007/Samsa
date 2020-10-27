package com.example.samsa.ui.launcher

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.samsa.BuildConfig
import com.example.samsa.R
import com.example.samsa.api.ApiHelper
import com.example.samsa.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_launcher)

        // Check for updates
        val pf = PreferenceManager.getDefaultSharedPreferences(this)
        if (!pf.getBoolean(getString(R.string.pref_key_check_for_updates), false)) {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    val releases = ApiHelper.createGithubService().getReleases()
                    if (releases.isNotEmpty() && releases[0].assets.isNotEmpty()) {
                        val tagName = releases[0].tagName
                        if (compareVersions(tagName, BuildConfig.VERSION_NAME) == true) {
                            showUpdateDialog(tagName, releases[0].body)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LauncherActivity, "failed to get update", Toast.LENGTH_LONG)
                        .show()
                } finally {
                    startMainActivity()
                }
            }
        } else {
            startMainActivity()
        }

    }

    private fun showUpdateDialog(version: String, body: String, delay: Long = 3500L) {
        AlertDialog.Builder(this).apply {
            setTitle("Version $version available@Github")
            setMessage(body)
        }.create().apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }.show()
        startMainActivity(delay)
    }

    private fun startMainActivity(delay: Long = 0L) {
        lifecycleScope.launch {
            delay(delay)
            startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
            this@LauncherActivity.finish()
        }
    }

    /**
     * Compares vx.x.x format
     *
     * @return returns true if param a is higher, null if invalid
     */
    private fun compareVersions(a: String, b: String): Boolean? {
        val versionRegex = Regex("v?(\\d+).(\\d+).(\\d+)")
        val aVersionMatch = versionRegex.find(a)
        val bVersionMatch = versionRegex.find(b)

        if (aVersionMatch != null && bVersionMatch != null) {
            val (u, v, w) = aVersionMatch.destructured
            val (x, y, z) = bVersionMatch.destructured

            if (u > x) {
                return true
            } else {
                if (u == x && v > y) {
                    return true
                } else if (v == y && w > z) {
                    return true
                }
                return false
            }
        } else {
            return null
        }
    }
}