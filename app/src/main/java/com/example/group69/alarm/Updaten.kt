package com.example.group69.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import android.content.Context
import org.jetbrains.anko.*
import android.support.v4.content.LocalBroadcastManager
import android.content.BroadcastReceiver
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
  
    override fun doInBackground(vararg activies : Object): Nothing? {
        Log.d("updaten","updaten start")
        var failcount = 0
        var iterationcount = 0

        //mac = activies[0] as MainActivity

        while (!isCancelled) {
            Log.d("updaten", "iteration #" + ++iterationcount)

            DeletePendingFinishedStock()
            Log.d("updaten", "1") //fails right here when trying to get from database
            var stocksTargets = getStocklistFromDB()
            Log.d("updaten", "2")
            Log.v("Updaten ", if (stocksTargets.isEmpty()) "might be empty list: " else
            "stocks targets: " + stocksTargets.map { it.ticker }.joinToString(", "))

            for (stockx in stocksTargets) {
                val ticker: String = stockx.ticker
                if (ticker.equals("snoozee")) {
                    Log.d("snooze","snoozin")
                    SetPendingFinishedStock(stockx.stockid)
                    Thread.sleep(1000 * stockx.target.toLong())
                    continue
                }

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

        DeletePendingFinishedStock()
        var stocksTargets = getStocklistFromDB()

        Log.v("Updaten ", if (stocksTargets.isEmpty()) "might be empty list: " else "stocks targets: " + stocksTargets.map { it.ticker }.joinToString(", "))

        for (stockx in stocksTargets) {
            val ticker: String = stockx.ticker

            var currPrice = if (stockx.crypto == 1L) { Geldmonitor.getCryptoPrice(ticker)
            } else { Geldmonitor.getStockPrice(ticker) }

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

        return failcount
        Log.d("updaten","service killed, finishing updaten")
        /*
        DatabaseManager.getInstance().database.close()  //getting rid of this makes stopping scan and restarting not crash
        */
        return null
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

    fun PriceBroadcastLocal(stockid: Long, currentprice: Double) {
        val intent = Intent("com.example.group69.alarm")
        intent.putExtra("stockid", stockid)
        intent.putExtra("currentprice", currentprice)
        LocalBroadcastManager.getInstance(this.TutorialServiceContext).sendBroadcast(intent)
    }
    private fun createBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("beforeAlarm","mangracina55")
                // val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                //val num : LongArray = longArrayOf(1000,1000,1000)
                //v.vibrate(num,3)
                //updat.cancel(true)
                //updat.pause(intent.getStringExtra("delay").toLong())
                //Log.d("slept","canceled " + intent.getStringExtra("delay"))

                //Thread.sleep(intent.getStringExtra("delay").toLong() * 6000)
                //Log.d("slept",intent.getStringExtra("delay"))
                //updat.execute("h")

                //deleteStockOfThisIndex(intent.getStringExtra("result"))
            }
        }
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