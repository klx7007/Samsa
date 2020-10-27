package com.example.samsa.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.samsa.R
import com.example.samsa.databinding.ActivityMainBinding
import com.example.samsa.ui.main.media.MediaFragment
import com.example.samsa.ui.main.postlist.PostListFragment
import kotlin.system.exitProcess


interface OnBackPressedListener {
    fun onBackPressed()
}

class MainActivity : AppCompatActivity() {
    var backKeyPressedTime: Long = 0
    private lateinit var binding: ActivityMainBinding
    private val fragmentManager = supportFragmentManager
    private val mViewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            application,
            intent.extras?.getString(
                "tagName"
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Activity
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.vm = mViewModel
        binding.lifecycleOwner = this


        //window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.container,
                PostListFragment.newInstance()
            ).commit()
        }

        // post click event listener
        mViewModel.postClickEvent.observe(this, Observer {
            if (!it.hasBeenHandled) {
                it.iHandledIt()
                fragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    .add(R.id.container, MediaFragment())
                    .addToBackStack(null)
                    .commit()
            }
        })

        // tag click event listener
        mViewModel.tagClickEvent.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra("tagName", it.tagName)
                })
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        if (isTaskRoot && fragmentManager.backStackEntryCount <= 0) {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis()
                Toast.makeText(this, getString(R.string.app_close_back_button), Toast.LENGTH_SHORT)
                    .show()
            } else {
                finish()
                exitProcess(0)
            }
        } else if (fragmentManager.backStackEntryCount > 0) {
            val fragments = supportFragmentManager.fragments
            for (f in fragments) {
                if (f is OnBackPressedListener) {
                    f.onBackPressed()
                }
            }
        } else {
            super.onBackPressed()
        }
    }
}