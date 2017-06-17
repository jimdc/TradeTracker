package com.example.group69.alarm

import org.jetbrains.anko.db.*
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import android.database.sqlite.*

/**
 * Created by james on 17/06/2017.
 */
data class Stock (val name: String) {
    var urgency: String = "low";
    var volatility: Int = 0;
    var targetprice: Int = 0;
}