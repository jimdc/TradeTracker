package com.advent.group69.tradetracker.view

import android.app.Activity
import android.os.Bundle
import com.advent.group69.tradetracker.R

/**
 * Called when the notification is clicked on in the taskbar
 */
class MoreInfoNotification : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.more_info_notific)
    }
}