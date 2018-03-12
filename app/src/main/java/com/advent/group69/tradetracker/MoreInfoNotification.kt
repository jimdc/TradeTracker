package com.advent.group69.tradetracker

import android.app.Activity
import android.os.Bundle

/**
 * It is called when the notification is clicked on in the taskbar
 */
class MoreInfoNotification : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.more_info_notific)
    }
}