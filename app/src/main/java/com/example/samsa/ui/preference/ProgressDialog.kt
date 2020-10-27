package com.example.samsa.ui.preference

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.example.samsa.R

class ProgressDialog(context: Context, private val progressText: String = "Loading...") :
    Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_progress)
        setCancelable(false)
        findViewById<TextView>(R.id.dialog_progress_text)?.text = progressText
    }
}