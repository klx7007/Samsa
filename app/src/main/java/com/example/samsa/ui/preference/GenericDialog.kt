package com.example.samsa.ui.preference

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.samsa.R

abstract class GenericDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_generic)

        val container = findViewById<RelativeLayout>(R.id.dialog_container)
        val pButton = findViewById<TextView>(R.id.positive_btn)
        val nButton = findViewById<TextView>(R.id.negative_btn)

        container.addView(getDialogContentView(container))

        pButton.setOnClickListener {
            onDialogPositiveClick()
            dismiss()
        }
        nButton.setOnClickListener {
            onDialogNegativeClick()
            dismiss()
        }
    }

    open fun onDialogPositiveClick() {}

    open fun onDialogNegativeClick() {}

    abstract fun getDialogContentView(container: ViewGroup): View
}