package com.advent.tradetracker.model

/**
 * SnoozeManager is check interval time agnostic. Whenever you call [isSnoozing] or [getSnoozeTimeRemaining] it refreshes.
 * So a service which queries SnoozeManager and a UI element that queries SnoozeManager, can have different intervals.
 */
object SnoozeManager {
    private var isSnoozing = false
    private var wakeupSystemTime = 0L
    private var snoozeTimeRemaining = 0L
    public var snoozeMsecTotal = 0L

    /**
     * @return false if snooze already running, unless ![forceRestart]
     */
    fun startSnooze(milliseconds: Long, forceRestart: Boolean): Boolean {
        if (isSnoozing and !forceRestart) return false

        wakeupSystemTime = System.currentTimeMillis() + milliseconds
        snoozeTimeRemaining = milliseconds
        isSnoozing = true

        return true
    }

    fun stopSnooze() {
        snoozeTimeRemaining = 0
        snoozeMsecTotal = 0
        isSnoozing = false
    }

    fun isSnoozing(): Boolean {
        getSnoozeTimeRemaining() //Refresh
        return isSnoozing
    }

    fun getSnoozeTimeRemaining(): Long {
        if (!isSnoozing) return 0L

        snoozeTimeRemaining = wakeupSystemTime - System.currentTimeMillis()
        if (snoozeTimeRemaining <= 0) stopSnooze()

        return snoozeTimeRemaining
    }
}