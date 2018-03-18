package com.advent.group69.tradetracker.model

interface SnoozeInterface {
    fun setSnoozeInfo(info: String)
    fun setMaxSnoozeProgress(progress: Int)
    fun setSnoozeProgress(progress: Int)
}