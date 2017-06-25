package com.example.group69.alarm

import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import android.database.sqlite.*
import android.database.sqlite.SQLiteDatabase

data class Stock (val stockid: Long, val ticker: String, var target: Double?, var above: Boolean, var phone: Boolean)