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
    var stocksTargets: List<Stock> = ArrayList()
    var stocksNow: MutableList<StockNow> = mutableListOf<StockNow>()
    var ctxx = ctx
    var i = 0
    var delStock: Long = 5
    var alarmPlayed: Boolean = false

    val manager: MySqlHelper = MySqlHelper.getInstance(this.ctxx)
    val database = manager.writableDatabase

    /**
     * at very start, load from database ONCE to fill values such as time duration for the scan (market open/close)
     * @todo break this down into modules, because there are also a lot of TODOs in here
     */
    override fun doInBackground(vararg tickers: String): Int? {

        var failcount = 0
        var iterationcount = 0

        while (!isCancelled) {
            Log.d("updaten", "iteration #" + iterationcount++)
            Log.d("Attempting Updaten", "Up")
            try {
                database.use {
                    if (alarmPlayed == true) {
                        database.delete(NewestTableName, "_stockid=$delStock")
                        alarmPlayed = false
                    }

                    stocksTargets = getStocklistFromDB();
                }
            } catch (e: android.database.sqlite.SQLiteException) {
                Log.d("In Updaten: ", "sqlLite error onCreate: " + e.toString())
            }

            if (!stocksTargets.isEmpty()) {
                var stocknamelist: List<CharSequence> = ArrayList()
                stocksTargets.forEach { i -> stocknamelist += i.toString() }
                Log.d("yeezy updaten ", "stocks targets: " + stocksTargets.toString())
            } else {
                Log.d("updaten ", "might be empty list: ")
            }

            for ((a, stockx) in stocksTargets.withIndex()) {
                //this needs to be tweaked as now it will recheck the same stock
                val ticker: String = stockx.ticker
                var currPrice = -2.0
                try {
                    currPrice = if (stockx.crypto == 1L) {
                        Geldmonitor.getCryptoPrice(ticker)
                    } else {
                        Geldmonitor.getStockPrice(ticker)
                    }
                    Log.d("Errorlog", "got the symb: " + ticker)
                    Log.d("Errorlog", "got the price: " + currPrice)
                } catch (e: Exception) {
                    currPrice = -3.0
                    //for now just checking to see if all stocks are returning -3.0
                    Log.d("Errorlog", "got stock " + stockx.ticker + " caused NPE!")
                }

                if (currPrice >= 0) {
                    Log.d("Errorlog", "not null")

                    if (
                            ((stockx.above == 1L) && (currPrice > stockx.target)) ||
                            ((stockx.above == 0L) && (currPrice < stockx.target))
                    ) {
                        alarmPlayed = true
                        Log.d("mangracina", "mangracina")
                        publishProgress(stockx.ticker, stockx.target.toString(), stockx.above.toString()) //return index so we know which stock to remove from database
                        delStock = stockx.stockid
                    }
                } else {
                    //if price is less than 0, we have an error from network, might be one stock.
                    //If it is all we will find out with:
                    failcount++
                }
            }
            if (failcount == stocksTargets.size) {
                Log.d("got", "network connection error, all stocks getting -3.0")
                try {
                    Thread.sleep(60000)
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                    Thread.currentThread().interrupt()
                }
            }
            failcount = 0
            //publishProgress("alert")
            try {
                Thread.sleep(8000)
            } catch (ie: InterruptedException) {
                ie.printStackTrace()
                Thread.currentThread().interrupt()
            }
        }
        Log.d("Errorlog", "got thread interupted")
        return 0
    }

    /**
     * Queries database [NewestTableName] for stocks list and returns it
     * @return the rows of stocks, or an empty list if database fails
     * @seealso [MainActivity.getStocklistFromDB], it should be identical
     */
    fun getStocklistFromDB() : List<Stock> {
        var results: List<Stock> = ArrayList()
        try {
            database.use {
                val sresult = database.select(NewestTableName, "_stockid", "ticker", "target", "ab", "phone", "crypto")

                sresult.exec {
                    if (count > 0) {
                        val parser = rowParser { stockid: Long, ticker: String, target: Double, above: Long, phone: Long, crypto: Long ->
                            Stock(stockid, ticker, target, above, phone, crypto)
                        }
                        results = parseList(parser)
                    }
                }
            }
        } catch (e: android.database.sqlite.SQLiteException) {
            Log.e("err gSlFDB: ", e.toString())
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

        Log.d("playAlarm", "" + ab)
        val alertTime = GregorianCalendar().timeInMillis + 5
        var gain: String
        if (ab == "1") {
            gain = "rose to"
        } else gain = "dropped to"

        val alertIntent = Intent(ctxx, AlertReceiver::class.java)
        val alert1: String
        alert1 = ticker.toUpperCase() + " " + gain + " " + Utility.toDollar(price)
        val alert2: String
        alert2 = Utility.toDollar(price)
        val alert3: String
        alert3 = ab
        alertIntent.putExtra("message1", alert1)
        alertIntent.putExtra("message2", alert2)
        alertIntent.putExtra("message3", alert3)

        val alarmManager = ctxx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(ctxx, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))

        val v = ctxx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val num: LongArray = longArrayOf(0, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500)
        v.vibrate(num, -1)


    }
}