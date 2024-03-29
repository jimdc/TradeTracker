package com.advent.tradetracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import org.jetbrains.anko.*
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent
import android.os.Vibrator
import android.support.v7.preference.PreferenceManager
import java.util.*
import com.advent.tradetracker.Utility.withDollarSignAndDecimal
import android.os.VibrationEffect
import android.os.Build
import com.advent.tradetracker.view.PriceAlertBroadcastReceiver


import timber.log.Timber


/**
 * Checks through [StockDownloader]
 */
class StockScanner(private val callerContext: Context) {
    private var stockToDeleteId: Long = 5
    private var alarmPlayed: Boolean = false
    private lateinit var dbFunctions: com.advent.tradetracker.model.DatabaseFunctions
    var isRunning = false

    fun startup() {
        dbFunctions = com.advent.tradetracker.model.DatabaseFunctions(callerContext)
    }

    fun cleanup() {
        dbFunctions.cleanup()
    }

    /**
     * Iterates through the stocks and sends alert if necessary
     */
  
    fun scanNetwork() {
        Timber.i("scanNetwork start")
        var failCount = 0

        BatteryAwareness.checkPowerStatusAndNotify(LocalBroadcastManager.getInstance(callerContext))

        deletePendingFinishedStock()

        val stocksTargets = dbFunctions.getStockList()
        Timber.v(if (stocksTargets.isEmpty()) "might be empty list: " else "stocks targets: " + stocksTargets.joinToString(", ") { it.ticker })

        for (stockx in stocksTargets) {

            val ticker: String = stockx.ticker

            val currentPrice = if (stockx.crypto == 1L) {
                com.advent.tradetracker.StockDownloader.getCryptoPrice(ticker)
            } else {
                com.advent.tradetracker.StockDownloader.getStockPrice(ticker)
            } //changed from getStockPrice, the livestockprice is often non-existent and doesnt round as well for penny stocks

            Timber.d(stockx.ticker + ", " + currentPrice.toString())

            if (currentPrice >= 0) {
                broadcastPriceLocally(stockx.stockid, currentPrice)
                Timber.v("currentPrice $currentPrice is not null")
                if(stockx.target>0) {
                    if (
                            ((stockx.above == 1L) && (currentPrice > stockx.target)) ||
                            ((stockx.above == 0L) && (currentPrice < stockx.target))
                    ) {
                        setPendingFinishedStock(stockx.stockid)
                        broadcastPriceGlobally(stockx.ticker, stockx.target.toString(), stockx.above.toString(), "regular")
                        continue
                    }
                }
                else if(stockx.trailingPercent > 0){ //trailing stop loss code here. this would need to be > 0
                    if(currentPrice <= stockx.stopLoss){
                        setPendingFinishedStock(stockx.stockid)
                        broadcastPriceGlobally(stockx.ticker, stockx.stopLoss.toString(), "b", "regular")
                        continue
                    }

                    if(currentPrice > stockx.highestPrice) {
                        Timber.d("activating")
                        stockx.highestPrice = currentPrice
                        dbFunctions.addOrEditStock(stockx)
                    }
                    if( !(stockx.activationPrice == -2.0 || stockx.activationPrice == -1.0) ) {
                        //an activation price of -2.0 denotes that it has already been activated, -1.0 denotes no activation price
                        if(currentPrice >= stockx.activationPrice) {  //activation price must be higher than the start price @ creation
                            stockx.activationPrice = -2.0 //we could make it so the database subscribes to the observable,
                            dbFunctions.addOrEditStock(stockx)
                        }
                    }
                    if( stockx.activationPrice == -2.0 || stockx.activationPrice == -1.0 ) {
                        if(currentPrice <= ( stockx.highestPrice * (100 - stockx.trailingPercent)/100 ) ) {
                            setPendingFinishedStock(stockx.stockid)
                            broadcastPriceGlobally(stockx.ticker, stockx.trailingPercent.toString(), "b", "trailing")
                            //have this send in the format of abc dropped x% from the highest point of Y
                        }

                    }

                }
            } else {
                Timber.v( "currentPrice $currentPrice < 0, ++failCount to " + ++failCount)
            }

        }

        if (failCount == stocksTargets.size) {
            Timber.e( "All stocks below zero. Connection error?")
        }
    }

    /**
     * @param[stockId] The stock you want to mark for [deletePendingFinishedStock]
     */
    private fun setPendingFinishedStock(stockId: Long) {
        alarmPlayed = true
        stockToDeleteId = stockId
        Timber.v( "scheduled deletion of stock $stockId")
    }

    private fun deletePendingFinishedStock() {
        if (alarmPlayed) {
            dbFunctions.deleteStockByStockId(stockToDeleteId)
            Timber.v( "deletePendingFinishedStock: requested DBS delete of $stockToDeleteId")
        }
    }

    private fun broadcastPriceLocally(stockId: Long, currentPrice: Double) {
        Timber.i( "Sending price update of $stockId as $currentPrice")
        val intent = Intent("PRICEUPDATE")

        intent.putExtra("stockid", stockId)
        intent.putExtra("currentprice", currentPrice)
        intent.putExtra("time", GregorianCalendar().time.toString())
        LocalBroadcastManager.getInstance(callerContext).sendBroadcast(intent)
    }

    /**
     * Create a 5sec alert message for what the ticker "rose to" or "dropped to"
     * To be passed on to [PriceAlertBroadcastReceiver]
     * Set the system service [alarmManager] as FLAG_UPDATE_CURRENT
     * @param[ticker] The relevant ticker
     * @param[price] The new, noteworthy price
     * @param[ab] "1" means above, something else like "0" means below
     */
    private fun broadcastPriceGlobally(ticker: String, price: String, ab: String, type: String) {

        Timber.v("Building tradetracker with ticker=$ticker, price=$price, ab=$ab")
        val alertTime = GregorianCalendar().timeInMillis + 5

        val alertIntent = Intent(callerContext, PriceAlertBroadcastReceiver::class.java)

        alertIntent
                .putExtra(callerContext.resources.getString(R.string.tickerRoseDroppedMsg), "${ticker.toUpperCase()} ${if (ab == "1") "rose to" else "dropped to ${price.withDollarSignAndDecimal()}"}")
                .putExtra(callerContext.resources.getString(R.string.tickerTargetPrice), price.withDollarSignAndDecimal())
                .putExtra(callerContext.resources.getString(R.string.aboveBelow), ab)
                .putExtra("type",type)

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
        if (sharedPreferences.getBoolean(callerContext.resources.getString(R.string.vibrate_key), true)) {
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
}