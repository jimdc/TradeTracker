package com.advent.group69.tradetracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import android.content.Context
import org.jetbrains.anko.*
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent
import android.os.Vibrator
import android.support.v7.preference.PreferenceManager
import java.util.*
import com.advent.group69.tradetracker.Utility.withDollarSignAndDecimal
import android.os.VibrationEffect
import android.os.Build



/**
 * Checks through [Geldmonitor]
 */
class Updaten(private val callerContext: Context) {
    private var stockToDeleteId: Long = 5
    private var alarmPlayed: Boolean = false
    var isRunning = false

    /**
     * Iterates through the stocks and sends alert if necessary
     */
  
    fun scanNetwork() {
        Log.d("Updaten","scanNetwork start")
        var failCount = 0

        if(powerSavingOn && !notifiedOfPowerSaving) notifyUserToTurnOffPowerSaving()

        deletePendingFinishedStock()

        val stocksTargets = dbFunctions.getStockList()
        Log.v("Updaten ", if (stocksTargets.isEmpty()) "might be empty list: " else "stocks targets: " + stocksTargets.joinToString(", ") { it.ticker })

        for (stockx in stocksTargets) {
            val ticker: String = stockx.ticker

            val currentPrice = if (stockx.crypto == 1L) { Geldmonitor.getCryptoPrice(ticker)
            } else { Geldmonitor.getLateStockPrice(ticker) } //changed from getStockPrice, the livestockprice is often non-existent and doesnt round as well for penny stocks

            if (currentPrice >= 0) {
                broadcastPriceLocally(stockx.stockid, currentPrice)
                Log.v("Updaten", "currentPrice $currentPrice is not null")
                if (
                        ((stockx.above == 1L) && (currentPrice > stockx.target)) ||
                        ((stockx.above == 0L) && (currentPrice < stockx.target))
                ) {
                    setPendingFinishedStock(stockx.stockid)
                    broadcastPriceGlobally(stockx.ticker, stockx.target.toString(), stockx.above.toString())
                }
            } else {
                Log.v("Updaten", "currentPrice $currentPrice < 0, ++failCount to " + ++failCount)
            }
        }

        if (failCount == stocksTargets.size) {
            Log.e("Updaten", "All stocks below zero. Connection error?")
        }
    }

    private fun notifyUserToTurnOffPowerSaving() {
        val intent = Intent("com.example.group69.alarm")
        intent.putExtra("stockid", 1111111111111111111)
        LocalBroadcastManager.getInstance(this.callerContext).sendBroadcast(intent)
    }

    /**
     * @param[stockId] The stock you want to mark for [deletePendingFinishedStock]
     */
    private fun setPendingFinishedStock(stockId: Long) {
        alarmPlayed = true
        stockToDeleteId = stockId
        Log.v("Updaten", "scheduled deletion of stock $stockId")
    }

    private fun deletePendingFinishedStock() {
        if (alarmPlayed) {
            dbFunctions.deleteStockByStockId(stockToDeleteId)
            Log.v("Updaten", "deletePendingFinishedStock: requested DBS delete of $stockToDeleteId")
        }
    }

    private fun broadcastPriceLocally(stockId: Long, currentPrice: Double) {
        Log.i("Updaten", "Sending price update of $stockId as $currentPrice")
        val intent = Intent("PRICEUPDATE")
        intent.putExtra("stockId", stockId)
        intent.putExtra("currentPrice", currentPrice)
        intent.putExtra("time", GregorianCalendar().time)
        LocalBroadcastManager.getInstance(callerContext).sendBroadcast(intent)
    }

    /**
     * Create a 5sec alert message for what the ticker "rose to" or "dropped to"
     * To be passed on to [AlertReceiver]
     * Set the system service [alarmManager] as FLAG_UPDATE_CURRENT
     * @param[ticker] The relevant ticker
     * @param[price] The new, noteworthy price
     * @param[ab] "1" means above, something else like "0" means below
     */
    private fun broadcastPriceGlobally(ticker: String, price: String, ab: String) {

        Log.v("Updaten", "Building tradetracker with ticker=$ticker, price=$price, ab=$ab")
        val alertTime = GregorianCalendar().timeInMillis + 5

        val alertIntent = Intent(callerContext, AlertReceiver::class.java)

        alertIntent
                .putExtra(callerContext.resources.getString(R.string.tickerRoseDroppedMsg), "${ticker.toUpperCase()} ${if (ab == "1") "rose to" else "dropped to ${price.withDollarSignAndDecimal()}"}")
                .putExtra(callerContext.resources.getString(R.string.tickerTargetPrice), price.withDollarSignAndDecimal())
                .putExtra(callerContext.resources.getString(R.string.aboveBelow), ab)

        val alarmManager = callerContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                alertTime,
                PendingIntent
                        .getBroadcast(callerContext, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        vibrate()
    }

    private fun vibrate() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(callerContext)
        if (sharedPreferences.getBoolean(callerContext.resources.getString(R.string.vibrate_key), true))
            shakeItBaby()
    }

    private fun shakeItBaby() {
        val vibrator = callerContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibratorPattern = longArrayOf(0, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500)

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibratorPattern, 10))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibratorPattern, -1)
        }
    }
}