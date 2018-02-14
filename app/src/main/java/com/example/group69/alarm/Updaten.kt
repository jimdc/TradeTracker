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
import android.content.Intent
import android.os.SystemClock
import android.os.Vibrator
import com.example.group69.alarm.Utility
import java.util.*


class Updaten(ctx: Context) : android.os.AsyncTask<String, String, Int>() {
    var ctxx = ctx
    var delStock: Long = 5
    var alarmPlayed: Boolean = false

    val manager: MySqlHelper = MySqlHelper.getInstance(this.ctxx)
    val database = manager.writableDatabase

    /**
     * @todo rewrite stocksTargets.withIndex loop so it does not recheck the same stock
     */
    override fun doInBackground(vararg tickers: String): Int? {
        var stocksTargets: List<Stock> = ArrayList()

        var failcount = 0
        var iterationcount = 0

        while (!isCancelled) {
            Log.d("updaten", "iteration #" + ++iterationcount)

            DeletePendingFinishedStock()
            stocksTargets = getStocklistFromDB()

            Log.v("Updaten ", if (stocksTargets.isEmpty()) "might be empty list: " else
            "stocks targets: " + stocksTargets.map { it.ticker }.joinToString(", "))

            for (stockx in stocksTargets) {
                val ticker: String = stockx.ticker

                var currPrice = if (stockx.crypto == 1L) { Geldmonitor.getCryptoPrice(ticker)
                } else { Geldmonitor.getStockPrice(ticker) }

                if (currPrice >= 0) {
                    Log.v("Updaten", "currPrice $currPrice is not null")
                    if (
                            ((stockx.above == 1L) && (currPrice > stockx.target)) ||
                            ((stockx.above == 0L) && (currPrice < stockx.target))
                    ) {
                        SetPendingFinishedStock(stockx.stockid)
                        publishProgress(stockx.ticker, stockx.target.toString(), stockx.above.toString())
                    }
                } else {
                    Log.v("Updaten", "currPrice $currPrice < 0, netErr? ++failcount to " + ++failcount)
                }
            }

            if (failcount == stocksTargets.size) {
                Log.e("Updaten", "All stocks below zero. Connection error?")
                TryToSleepFor(60000)
            }

            failcount = 0
            TryToSleepFor(8000)
        }

        Log.d("Updaten", "doInBackground thread interrupted")
        return 0
    }

    fun SetPendingFinishedStock(stockid: Long) {
        alarmPlayed = true
        delStock = stockid
        Log.v("Updaten", "scheduled deletion of stock $stockid")
    }

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
     * Reduces repetitive try-catch blocks for sleep.
     * Prints stack trace and interrupts thread
     */
    private fun TryToSleepFor(milliseconds: Long) {
        try {
            Thread.sleep(milliseconds)
        } catch (ie: InterruptedException) {
            ie.printStackTrace()
            Thread.currentThread().interrupt()
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

    /**
     * isSleepTime true means: scanning is paused and will resume at specified start time, go to settings to change start time
     */
    fun SleepUntilMarketReopens() {

        var timeCount: Long = 0
        val date = Date()

        GregorianCalendar().time
        val cal = Calendar.getInstance()
        cal.setTimeZone(TimeZone.getTimeZone("America/New_York"))
        cal.time = date
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        Log.d("hours", "hours1 " + hours.toString())

        var isSleepTime = false

        if(isSleepTime) {
            val hour = cal.get(Calendar.HOUR_OF_DAY)

            val starthour = 8
            val startmin = 58

            if (hour in 18..23) {
                Log.d("got sleep", "sleeping for " + (26 - hour) + " hours")
                Thread.sleep((26 - hour).toLong() * 60 * 60000 - 60000 * 5)
            } else if (hour in 0..1) {
                Log.d("got sleep", "sleeping for " + (2 - hour) + " hours")
                Thread.sleep((2 - hour).toLong() * 60 * 60000 - 60000 * 5)
            }

            //at this point, when accounting for DST, time can be 1:00 or 1:59, as well as
            // 3:00 or 3:59 (in which case we wait 1 minute if we wanted the 4am trading
            //if user started scan at say 7 and wants to wait until 9 that will also work
            if (cal.get(Calendar.HOUR_OF_DAY) != starthour + 1 || !(cal.get(Calendar.HOUR_OF_DAY) == starthour && cal.get(Calendar.MINUTE) >= startmin)) { //if 8:58 or 8:59.. skip this

                if (cal.get(Calendar.HOUR_OF_DAY) != 8) {
                    Log.d("got sleep", "sleeping for " + (starthour - cal.get(Calendar.HOUR_OF_DAY)) + " hours")
                    Thread.sleep((starthour - cal.get(Calendar.HOUR_OF_DAY)).toLong() * 60 * 60000)
                }

                if (cal.get(Calendar.MINUTE) != 59 && cal.get(Calendar.MINUTE) != 58) {
                    Log.d("got sleep", "sleeping for " + (startmin - cal.get(Calendar.MINUTE)) + " minutes")
                    Thread.sleep((startmin - cal.get(Calendar.MINUTE)).toLong() * 60000 + 10)
                }
            }

            isSleepTime = false
        }

        if(timeCount % 30 == 0L) {
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            if(hour !in 9..17){ //send broadcast or signal for a toast to go off which lets user know that
                isSleepTime = true
            }
        }

        timeCount++
    }

    /**
     * @todo should send the stock, price, and number in Intent so we know which to delete on the UI display
     * @todo use the LocalBroadcastManager later on to update the UI display
     */
    override fun onProgressUpdate(vararg progress: String) {
        Log.d("mangracina", "playing alarm")
        playAlarm(progress[0], progress[1], progress[2])
        val intent = Intent("com.example.group69.alarm")
        intent.putExtra(progress[0], progress[1])
        LocalBroadcastManager.getInstance(this.ctxx).sendBroadcast(intent)
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

        val alertIntent = Intent(ctxx, AlertReceiver::class.java)
        alertIntent.putExtra("message1", ticker.toUpperCase() + " " +
                if (ab == "1") "rose to" else "dropped to" + " " + Utility.toDollar(price))
        alertIntent.putExtra("message2", Utility.toDollar(price))
        alertIntent.putExtra("message3", ab)

        val alarmManager = ctxx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(ctxx, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))

        val v = ctxx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val num: LongArray = longArrayOf(0, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500)
        v.vibrate(num, -1)
    }
}