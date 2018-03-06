package com.example.group69.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import android.content.Context
import org.jetbrains.anko.*
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent
import android.os.Vibrator
import java.util.*


class Updaten(CallerContext: Context) {
    var TutorialServiceContext = CallerContext

    var IdOfStockToDelete: Long = 5
    var alarmPlayed: Boolean = false
    var running: Boolean = false

    /**
     * Iterates through the stocks found by querying [getStocklistFromDB]
     * If current price meets criteria, send to [onProgressUpdate] for alarm
     * If [Geldmonitor] functions return negative (error) for all stocks, err
     * @return 0 on success, more is the amount of milliseconds to sleep
     * @todo use flowable [dbFunctions]
     */
  
    fun scannetwork(vararg activies : Object): Long {
        Log.d("Updaten","scannetwork start")
        var failcount = 0

        DeletePendingFinishedStock()
        var stocksTargets = dbFunctions.getStocklistFromDB()

        Log.v("Updaten ", if (stocksTargets.isEmpty()) "might be empty list: " else "stocks targets: " + stocksTargets.map { it.ticker }.joinToString(", "))

        for (stockx in stocksTargets) {
            val ticker: String = stockx.ticker

            var currPrice = if (stockx.crypto == 1L) { Geldmonitor.getCryptoPrice(ticker)
            } else { Geldmonitor.getLateStockPrice(ticker) } //changed from getStockPrice, the livestockprice is often non-existent and doesnt round as well for penny stocks

            if (currPrice >= 0) {
                PriceBroadcastLocal(stockx.stockid, currPrice)
                Log.v("Updaten", "currPrice $currPrice is not null")
                if (
                        ((stockx.above == 1L) && (currPrice > stockx.target)) ||
                        ((stockx.above == 0L) && (currPrice < stockx.target))
                ) {
                    SetPendingFinishedStock(stockx.stockid)
                    AlarmBroadcastGlobal(stockx.ticker, stockx.target.toString(), stockx.above.toString())
                }
            } else {
                Log.v("Updaten", "currPrice $currPrice < 0, netErr? ++failcount to " + ++failcount)
            }
        }

        if (failcount == stocksTargets.size) {
            Log.e("Updaten", "All stocks below zero. Connection error?")
        }

        return 0
    }

    /**
     * @param[stockid] The stock you want to mark for [DeletePendingFinishedStock]
     */
    fun SetPendingFinishedStock(stockid: Long) {
        alarmPlayed = true
        IdOfStockToDelete = stockid
        Log.v("Updaten", "scheduled deletion of stock $stockid")
    }

    /**
     * If [alarmPlayed] is true, delete [IdOfStockToDelete] from Datenbank.
     * @todo Update the UI as well, perhaps through [OnProgressUpdate] ?
     */
    private fun DeletePendingFinishedStock() {
        if (alarmPlayed == true) {
            dbFunctions.deletestockInternal(IdOfStockToDelete)
            Log.v("Updaten", "DeletePendingFinishedStock: requested DBS delete of $IdOfStockToDelete")
        }
    }

    fun PriceBroadcastLocal(stockid: Long, currentprice: Double) {
        val intent = Intent("com.example.group69.alarm")
        intent.putExtra("stockid", stockid)
        intent.putExtra("currentprice", currentprice)
        intent.putExtra("time", GregorianCalendar().time.toLocaleString())
        LocalBroadcastManager.getInstance(this.TutorialServiceContext).sendBroadcast(intent)
    }

    /**
     * Create a 5sec alert message for what the ticker "rose to" or "dropped to"
     * Set the system service [alarmManager] as FLAG_UPDATE_CURRENT
     * @param[ticker] The relevant ticker
     * @param[price] The new, noteworthy price
     * @param[ab] "1" means above, something else like "0" means below
     */
    fun AlarmBroadcastGlobal(ticker: String, price: String, ab: String) {

        Log.v("Updaten", "Building alarm with ticker=$ticker, price=$price, ab=$ab")
        val alertTime = GregorianCalendar().timeInMillis + 5

        val alertIntent = Intent(TutorialServiceContext, AlertReceiver::class.java)
        alertIntent.putExtra("message1", ticker.toUpperCase() + " " +
                if (ab == "1") "rose to" else "dropped to" + " " + Utility.toDollar(price))
        alertIntent.putExtra("message2", Utility.toDollar(price))
        alertIntent.putExtra("message3", ab)

        val alarmManager = TutorialServiceContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(TutorialServiceContext, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))

        val v = TutorialServiceContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val num: LongArray = longArrayOf(0, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500)
        v.vibrate(num, -1)
    }
}