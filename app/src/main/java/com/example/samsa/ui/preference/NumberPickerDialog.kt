package com.example.samsa.ui.preference

import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import com.example.samsa.R

class NumberPickerDialog(private val frag: Fragment, private val currentNum: Int) :
    GenericDialog(frag.requireContext()) {

    internal lateinit var listener: NumberPickerDialogListener
    lateinit var numberPicker: NumberPicker

    interface NumberPickerDialogListener {
        fun onNumberPickerDialogPositiveClick(currentNum: Int) {}
        fun onNumberPickerDialogNegativeClick() {}
    }

    override fun onDialogPositiveClick() =
        listener.onNumberPickerDialogPositiveClick(numberPicker.value)

    override fun getDialogContentView(container: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.dialog_numerpicker, container, false)
        numberPicker = view.findViewById(R.id.dialog_numberpicker)
        numberPicker.minValue = 1
        numberPicker.maxValue = 3

        if (currentNum in 1..3)
            numberPicker.value = currentNum
        else
            numberPicker.value = 1

        return view
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = frag as NumberPickerDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (frag.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }
}