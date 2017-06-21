package com.example.group69.alarm

import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import android.database.sqlite.*
import android.database.sqlite.SQLiteDatabase

data class Stock (val name: String) {
    var urgency: String = "low"
    var volatility: Int = 0
    var targetprice: Int = 0
}