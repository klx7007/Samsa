package com.example.samsa.ui.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

object ViewAnimation {
    fun viewGoneAnimation(view: View) {
        view.animate()
            .alpha(0.0f)
            .setDuration(80)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    view.visibility = View.GONE
                }
            })
    }
}