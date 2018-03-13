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
import java.util.concurrent.TimeUnit

/**
 * Checks through [Geldmonitor]
 */
class Updaten(CallerContext: Context) {
    var TutorialServiceContext = CallerContext

    var IdOfStockToDelete: Long = 5
    var alarmPlayed: Boolean = false
    var running: Boolean = false

    /**
     * Need a way to add the delay element here.
     */
    fun RXscannetwork() {

        /*
        dbFunctions.getFlowableStocklist().debounce(8000, TimeUnit.MILLISECONDS).subscribe{
            it.forEach {
                var currPrice = with (Geldmonitor) {
                    if (it.crypto == 1L) getCryptoPrice(it.ticker) else getLateStockPrice(it.ticker)
                }
                if (currPrice > 0) {
                    PriceBroadcastLocal(it.stockid, currPrice)

                    if (it.above==1L && currPrice>it.target || it.above==0L && currPrice<it.target) {
                        SetPendingFinishedStock(it.stockid)
                        AlarmBroadcastGlobal(it.ticker, it.target.toString(), it.above.toString())
                    }
                }
            }
        }
        */
    }

    /**
     * Iterates through the stocks found by querying [getStocklistFromDB]
     * If current price meets criteria, send to [onProgressUpdate] for tradetracker
     * If [Geldmonitor] functions return negative (error) for all stocks, err
     * @return 0 on success, more is the amount of milliseconds to sleep
     * @todo use flowable [dbFunctions]
     */
  
    fun scannetwork(vararg activies : Object) {

        //val v = TutorialServiceContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        //val num: LongArray = longArrayOf(400, 400, 400)
        //v.vibrate(num, -1)
        Log.d("Updaten","scannetwork start")
        var failcount = 0

        if(powerSavingOn && !notifiedOfPowerSaving){
            powerSavingFun()
        }

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
    }

    fun powerSavingFun() {
        val intent = Intent("com.example.group69.alarm")
        intent.putExtra("stockid", 1111111111111111111)
        LocalBroadcastManager.getInstance(this.TutorialServiceContext).sendBroadcast(intent)
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
        Log.i("Updaten", "Sending price update of ${stockid} as ${currentprice}")
        val intent = Intent("PRICEUPDATE")
        intent.putExtra("stockid", stockid)
        intent.putExtra("currentprice", currentprice)
        intent.putExtra("time", GregorianCalendar().time.toLocaleString())
        LocalBroadcastManager.getInstance(this.TutorialServiceContext).sendBroadcast(intent)
    }

    /**
     * Create a 5sec alert message for what the ticker "rose to" or "dropped to"
     * To be passed on to [AlertReceiver]
     * Set the system service [alarmManager] as FLAG_UPDATE_CURRENT
     * @param[ticker] The relevant ticker
     * @param[price] The new, noteworthy price
     * @param[ab] "1" means above, something else like "0" means below
     */
    fun AlarmBroadcastGlobal(ticker: String, price: String, ab: String) {

        Log.v("Updaten", "Building tradetracker with ticker=$ticker, price=$price, ab=$ab")
        val alertTime = GregorianCalendar().timeInMillis + 5

        val alertIntent = Intent(TutorialServiceContext, AlertReceiver::class.java)

        alertIntent.putExtra(TutorialServiceContext.resources.getString(R.string.tickerRoseDroppedMsg),
                ticker.toUpperCase() + " " + if (ab == "1") "rose to" else "dropped to" + " " + Utility.toDollar(price))
        alertIntent.putExtra(TutorialServiceContext.resources.getString(R.string.tickerTargetPrice),
                Utility.toDollar(price))
        alertIntent.putExtra(TutorialServiceContext.resources.getString(R.string.aboveBelow),
                ab)

        val alarmManager = TutorialServiceContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(TutorialServiceContext, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
        vibrate()
    }

    fun vibrate() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(TutorialServiceContext)
        val vibratePref = sharedPref.getBoolean(TutorialServiceContext.resources.getString(R.string.vibrate_key), true)
        if (vibratePref) {
            val v = TutorialServiceContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val num: LongArray = longArrayOf(0, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500)
            v.vibrate(num, -1)
        }
    }
}