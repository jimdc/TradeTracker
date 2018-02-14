package com.example.group69.alarm


import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import android.content.Context
import org.jetbrains.anko.db.*
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.content.Intent
import android.os.Vibrator
import java.util.*


class Updaten(CallerContext: Context) : android.os.AsyncTask<String, String, Int>() {
    var TutorialServiceContext = CallerContext
    var delStock: Long = 5
    var alarmPlayed: Boolean = false

    val manager: SQLiteSingleton = SQLiteSingleton.getInstance(this.TutorialServiceContext)
    val database = manager.writableDatabase

    /**
     * @todo rewrite stocksTargets.withIndex loop so it does not recheck the same stock
     */
    override fun doInBackground(vararg tickers: String): Int? {

        var failcount = 0
        var iterationcount = 0

        while (!isCancelled) {
            Log.d("updaten", "iteration #" + ++iterationcount)

            DeletePendingFinishedStock()
            var stocksTargets = getStocklistFromDB()

            Log.v("Updaten ", if (stocksTargets.isEmpty()) "might be empty list: " else
            "stocks targets: " + stocksTargets.map { it.ticker }.joinToString(", "))

            for (stockx in stocksTargets) {
                val ticker: String = stockx.ticker

                var currPrice = if (stockx.crypto == 1L) { Geldmonitor.getCryptoPrice(ticker)
                } else { Geldmonitor.getStockPrice(ticker) }

                if (currPrice >= 0) {
                    publishProgress("Currprice2UIPlease", stockx.stockid.toString(), currPrice.toString())
                    Log.v("Updaten", "currPrice $currPrice is not null")
                    if (
                            ((stockx.above == 1L) && (currPrice > stockx.target)) ||
                            ((stockx.above == 0L) && (currPrice < stockx.target))
                    ) {
                        SetPendingFinishedStock(stockx.stockid)
                        publishProgress("AlarmPlease", stockx.ticker, stockx.target.toString(), stockx.above.toString())
                    }
                } else {
                    Log.v("Updaten", "currPrice $currPrice < 0, netErr? ++failcount to " + ++failcount)
                }
            }

            if (failcount == stocksTargets.size) {
                Log.e("Updaten", "All stocks below zero. Connection error?")
                Utility.TryToSleepFor(60000)
            }

            failcount = 0
            Utility.TryToSleepFor(8000)
        }

        Log.d("Updaten", "doInBackground thread interrupted")
        return 0
    }

    fun SetPendingFinishedStock(stockid: Long) {
        alarmPlayed = true
        delStock = stockid
        Log.v("Updaten", "scheduled deletion of stock $stockid")
    }

    /**
     * Just deleting it from the database doesn't update the UI.
     */
    private fun DeletePendingFinishedStock() {
        try {
            if (alarmPlayed == true) {
                database.delete(NewestTableName, "_stockid=$delStock")
                alarmPlayed = false
                Log.v("Updaten", "deleted completed stock $delStock")
            }
        } catch (e: android.database.sqlite.SQLiteException) {
            Log.e("Updaten", "could not delete $delStock: " + e.toString())
        }
    }

    /**
     * Queries database [NewestTableName] for stocks list and returns it
     * @return the rows of stocks, or an empty list if database fails
     * @seealso [MainActivity.getStocklistFromDB], which uses database.use that closes the DB
     * This thread doesn't do that because it could conflict with [DeletePendingFinishedStock]
     */
    fun getStocklistFromDB() : List<Stock> {
        var results: List<Stock> = ArrayList()
        try {
            val sresult = database.select(NewestTableName, "_stockid", "ticker", "target", "ab", "phone", "crypto")

            sresult.exec {
                if (count > 0) {
                    val parser = rowParser { stockid: Long, ticker: String, target: Double, above: Long, phone: Long, crypto: Long ->
                        Stock(stockid, ticker, target, above, phone, crypto)
                    }
                    results = parseList(parser)
                }
            }
        } catch (e: android.database.sqlite.SQLiteException) {
            Log.e("Updaten", "couldn' get stock list from DB: " + e.toString())
        }

        return results
    }


    override fun onProgressUpdate(vararg progress: String) {
        if (progress[0].equals("AlarmPlease")) { AlarmPlease(progress[1], progress[2], progress[3]) }
        else if (progress[0].equals("Currprice2UIPlease")) { Currprice2UIPlease(progress[1], progress[2]) }
    }

    fun Currprice2UIPlease(stockid: String, price: String) {

    }

    fun AlarmPlease(ticker: String, price: String, ab: String) {
        playAlarm(ticker, price, ab)

        val intent = Intent("com.example.group69.alarm")
        intent.putExtra(ticker, price)
        LocalBroadcastManager.getInstance(this.TutorialServiceContext).sendBroadcast(intent)
    }

    /**
     * Create a 5sec alert message for what the ticker "rose to" or "dropped to"
     * Set the system service [alarmManager] as FLAG_UPDATE_CURRENT
     * @param[ticker] The relevant ticker
     * @param[price] The new, noteworthy price
     * @param[ab] "1" means above, something else like "0" means below
     */
    fun playAlarm(ticker: String, price: String, ab: String) {

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