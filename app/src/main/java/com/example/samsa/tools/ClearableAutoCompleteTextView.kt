package com.example.samsa.tools

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import com.example.samsa.R


class ClearableAutoCompleteTextView : AppCompatAutoCompleteTextView {
    var justCleared = false

    var imgClearButton = ResourcesCompat.getDrawable(
        resources, R.drawable.ic_baseline_clear_24, null
    )

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI or imeOptions
        dropDownVerticalOffset =
            resources.getDimensionPixelSize(R.dimen.searchBar_editText_vertical_margin)

        this.doAfterTextChanged {
            if (!it.isNullOrEmpty())
                this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgClearButton, null)
            else
                this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }

        setOnTouchListener(object : OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                val et = this@ClearableAutoCompleteTextView

                if (et.compoundDrawables[2] == null)
                    return false

                if (p1?.action != MotionEvent.ACTION_UP)
                    return false

                if (p1.x > et.width - et.paddingRight - imgClearButton!!.intrinsicWidth) {
                    et.setText("")
                    justCleared = true
                }
                return false
            }
        })
    }

    override fun enoughToFilter(): Boolean {
        return true
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (focused && text.isBlank() && adapter != null) {
            performFiltering(text, 0)
            showDropDown()
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing && event?.action == MotionEvent.ACTION_UP) {
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if (inputManager.hideSoftInputFromWindow(
                    findFocus().windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            ) {
                return true
            } else {
                this.clearFocus()
                return false
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !isPopupShowing) {
            this.clearFocus()
        }
        return super.onKeyPreIme(keyCode, event)
    }
}