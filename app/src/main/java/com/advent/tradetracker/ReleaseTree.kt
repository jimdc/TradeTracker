package com.advent.tradetracker

import android.util.Log.*
import com.crashlytics.android.Crashlytics
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority == ERROR || priority == WARN)
            Crashlytics.log(priority, tag, message)
    }
}