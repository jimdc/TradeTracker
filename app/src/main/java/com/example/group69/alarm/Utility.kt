package com.example.group69.alarm

import android.view.View
import android.view.ViewGroup
import android.view.View.MeasureSpec
import android.widget.ListAdapter
import android.widget.ListView

object Utility {
    /**
     * Formats plain number to add dollar sign, trailing zero, and comma separators
     * @param[d] A string from Double, unformatted except for decimal point
     * @return transformed (lengthened) String
     */
    public fun toDollar(d: String): String {
        var s = d
        val dot = s.indexOf('.')

        if (dot + 1 == s.length - 1) {
            s += '0'
        }

        val sub = s.substring(0, dot)
        val str = StringBuilder(sub)

        if (sub.length > 3) {
            str.insert(sub.length - 3, ',')
        }

        return '$' + str.toString() + s.substring(dot, s.length)
    }

}