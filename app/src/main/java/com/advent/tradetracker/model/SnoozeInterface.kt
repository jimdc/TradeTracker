package com.advent.tradetracker.model

interface SnoozeInterface {
    fun setSnoozeInfo(info: String)
    fun setMaxSnoozeProgress(progress: Int)
    fun setSnoozeProgress(progress: Int)
}