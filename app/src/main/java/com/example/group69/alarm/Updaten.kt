package com.example.group69.alarm


import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import android.content.Context
import org.jetbrains.anko.*
import org.jetbrains.anko.db.*
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import android.support.v4.content.LocalBroadcastManager
import android.database.sqlite.SQLiteException
import android.content.Intent
import android.os.Vibrator
import java.util.*


class Updaten(CallerContext: Context) {
    var TutorialServiceContext = CallerContext
    var mac: MainActivity? = null

    var IdOfStockToDelete: Long = 5
    var alarmPlayed: Boolean = false
    var running: Boolean = false

    /**
     * Iterates through the stocks found by querying [getStocklistFromDB]
     * If current price meets criteria, send to [onProgressUpdate] for alarm
     * If [Geldmonitor] functions return negative (error) for all stocks, err
     * @todo send current price to [onProgressUpdate] to update the UI as well
     * @todo Reduce calls to [getStocklistFromDB] by signalling if DB changed
     */
    fun scannetwork(): Int {

        var failcount = 0

        DeletePendingFinishedStock()
        var stocksTargets = getStocklistFromDB()

        Log.v("Updaten ", if (stocksTargets.isEmpty()) "might be empty list: " else "stocks targets: " + stocksTargets.map { it.ticker }.joinToString(", "))

        for (stockx in stocksTargets) {
            val ticker: String = stockx.ticker

            var currPrice = if (stockx.crypto == 1L) { Geldmonitor.getCryptoPrice(ticker)
            } else { Geldmonitor.getStockPrice(ticker) }

            if (currPrice >= 0) {
                mac?.adapter?.setCurrentPrice(stockx.stockid, stockx.target)
                Log.v("Updaten", "currPrice $currPrice is not null")
                if (
                        ((stockx.above == 1L) && (currPrice > stockx.target)) ||
                        ((stockx.above == 0L) && (currPrice < stockx.target))
                ) {
                    SetPendingFinishedStock(stockx.stockid)
                    AlarmPlease(stockx.ticker, stockx.target.toString(), stockx.above.toString())
                }
            } else {
                Log.v("Updaten", "currPrice $currPrice < 0, netErr? ++failcount to " + ++failcount)
            }
        }

        if (failcount == stocksTargets.size) {
            Log.e("Updaten", "All stocks below zero. Connection error?")
        }

        return failcount
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
            if (dbsBound) {
                dbService.deletestockInternal(IdOfStockToDelete)
                Log.v("Updaten", "DeletePendingFinishedStock: requested DBS delete of $IdOfStockToDelete")
            } else {
                Log.e("Updaten", "DeletePendingFinishedStock: dbsBound = false, so did nothing.")
            }
        }
    }

    /**
     * Queries Datenbank [NewestTableName] for stocks list and returns it
     * @return the rows of stocks, or an empty list if Datenbank fails
     * @seealso [MainActivity.getStocklistFromDB], which uses Datenbank.use that closes the DB
     * This thread doesn't do that because it could conflict with [DeletePendingFinishedStock]
     */
    fun getStocklistFromDB() : List<Stock> {
        if (dbsBound) {
            return dbService.getStocklistFromDB()
        }
        Log.e("Updaten", "getStocklistFromDB: dbsBound = false, so did nothing.")
        return emptyList()
    }

    fun AlarmPlease(ticker: String, price: String, ab: String) {
        BroadcastSystemAlarm(ticker, price, ab)

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
    fun BroadcastSystemAlarm(ticker: String, price: String, ab: String) {

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