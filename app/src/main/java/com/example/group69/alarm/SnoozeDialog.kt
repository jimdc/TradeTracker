package com.example.group69.alarm

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.NumberPicker

//@todo implement with https://medium.com/@JakobUlbrich/building-a-settings-screen-for-android-part-3-ae9793fd31ec
//seealso https://developer.android.com/guide/topics/ui/settings.html#DefiningPrefs

class SnoozeDialog(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    private lateinit var mPicker: NumberPicker
    private var mNumber: Int? = 0

    init {
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
    }

    override fun onCreateDialogView(): View {
        mPicker = NumberPicker(context)
        mPicker.minValue = 1
        mPicker.maxValue = 100
        mPicker.value = mNumber!!
        return mPicker
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            // needed when user edits the text field and clicks OK
            mPicker.clearFocus()
            setValue(mPicker.value)
        }
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any) {
        setValue(if (restoreValue) getPersistedInt(mNumber!!) else defaultValue as Int)
    }

    fun setValue(value: Int) {
        if (shouldPersist()) {
            persistInt(value)
        }

        if (value != mNumber) {
            mNumber = value
            notifyChanged()
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }
}