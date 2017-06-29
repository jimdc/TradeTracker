package com.example.group69.alarm

import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import android.database.sqlite.*
import android.database.sqlite.SQLiteDatabase

data class Stock (val stockid: Long = 1337,
                  val ticker: String = "BABA",
                  var target: Double = 4.20,
                  var above: Long = 1,
                  var phone: Long = 0)