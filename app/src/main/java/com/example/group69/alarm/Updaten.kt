package com.example.group69.alarm


import android.app.AlarmManager
import android.app.PendingIntent
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.View
import android.content.Context
import org.jetbrains.anko.db.*
import org.jetbrains.anko.*
import android.support.annotation.MainThread
import com.example.group69.alarm.MySqlHelper.Companion.getInstance
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import org.jetbrains.anko.toast
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent
import java.util.*
import java.lang.NullPointerException


class Updaten(ctx: Context) : android.os.AsyncTask<String, String, Int>() {
    var stocksTargets : List<Stock> = ArrayList()
    var stocksNow: MutableList<StockNow> = mutableListOf<StockNow>()
    var ctxx = ctx
    var i = 0
    var delStock: Long = 5
    var alarmPlayed : Boolean = false
    override fun doInBackground(vararg tickers: String): Int? {

        while(true){
            var manager: MySqlHelper = MySqlHelper.getInstance(this.ctxx)
            val database = manager.writableDatabase
            //val sqlDB = MySqlHelper(ctxx)

            Log.d("Attempting Updaten", "Updaten")
            try {
                database.use {
                    if(alarmPlayed == true) {
                        database.delete("Portefeuille", "_stockid=$delStock")
                        alarmPlayed = false
                    }
                    val sresult = database.select("Portefeuille", "_stockid", "ticker", "target", "ab", "phone")
                    sresult.exec() {
                        if (count > 0) {
                            val parser = rowParser {
                                stockid: Long, ticker: String, target: Double, above: Long, phone: Long ->
                                Stock(stockid, ticker, target, above, phone)
                            }
                            stocksTargets = parseList(parser)

                        }

                    }

                }
            } catch (e: android.database.sqlite.SQLiteException) {
                Log.d("In Updaten: ", "error onCreate: " + e.toString())

            }
            if (!stocksTargets.isEmpty()) {

                var stocknamelist: List<CharSequence> = ArrayList()
                stocksTargets.forEach { i -> stocknamelist += i.toString() }
                Log.d("yeezy updaten ", "stocks targets: " + stocksTargets.toString())

            } else {
                Log.d("updaten ", "might be empty list: ")
            }

            var a = 0
            for (stockx in stocksTargets) { //this needs to be tweaked as now it will recheck the same stock
                //val stock = yahoofinance.YahooFinance.get(stockx.ticker.toString()); //this will keep checking duplicates with

                val ticker: String = stockx.ticker.toString()
                //the yahoo api when it could save time by using the saved value for that stock in a table
                var currPrice : Double? = null
                try {
                    //currPrice = stock.quote.price.toDouble()
                    Log.d("Errorlog", "got the symb: " + ticker)
                    currPrice = Geldmonitor.getPrice(ticker)
                    Log.d("Errorlog", "got the price: " + currPrice)
                } catch (e: Exception) {
                    currPrice = null
                    Log.d("Errorlog", "stock " + stockx.ticker.toString() + " caused NPE!")
                }
                if (currPrice != null) {
                    Log.d("Errorlog", "not null")
                    if (stockx.above == 1L) {
                        if (currPrice >= stockx.target) { //will need to DELETE THE ALARMPLAYED
                            alarmPlayed = true;
                            Log.d("mangracina", "mangracina")
                            publishProgress(stockx.ticker.toString(), stockx.target.toString(),stockx.above.toString()) //return index so we know which stock to remove from database
                            delStock = stockx.stockid


                        }
                    }
                    else {
                        if (currPrice <= stockx.target) {
                            alarmPlayed = true;
                            Log.d("mangracina", "playAlarm")
                            publishProgress(stockx.ticker.toString(), stockx.target.toString(),stockx.above.toString()) //return index so we know which stock to remove from database
                            delStock = stockx.stockid
                            /*var delStock = stockx.stockid
                            try {
                                database.delete("Portefeuille", "_stockid=$delStock")
                            } catch (e: android.database.sqlite.SQLiteException) {
                                Log.d("In Updaten: ", "error onCreate: " + e.toString())
                            } */
                        }
                    }
                }

                a++
            }
            //publishProgress("alert")
            try {
                Thread.sleep(10000)
            } catch (ie: InterruptedException) {
                ie.printStackTrace()
                Thread.currentThread().interrupt()
            }

        }

    }

    override fun onProgressUpdate(vararg progress: String) {
        Log.d("mangracina", "playing alarm")
        playAlarm(progress[0], progress[1], progress[2])

        val intent = Intent("com.example.group69.alarm")
        intent.putExtra(progress[0],progress[1]) //should send the stock, price, and number so we know which to delete on the UI display
        LocalBroadcastManager.getInstance(this.ctxx).sendBroadcast(intent) //we can use this later on for updating the UI display
    }

    inner class MyUndoListener : View.OnClickListener {

        override fun onClick(v: View) {

            // Code to undo the user's last action
        }
    }
    fun playAlarm(ticker: String, price: String, ab: String) {

        Log.d("playAlarm","" + ab)
        // Define a time value of 5 seconds
        val alertTime = GregorianCalendar().timeInMillis + 5
        val gain: String
        // Define our intention of executing AlertReceiver
        if(ab == "1"){
            gain = "rose to"
        }
        else gain = "dropped to"

        val alertIntent = Intent(ctxx, AlertReceiver::class.java)
        val alert1 : String
        alert1 = ticker + " " + gain + " " + price
        val alert2 : String
        alert2 = price
        val alert3 : String
        alert3 = ab
        alertIntent.putExtra("message1", alert1)
        alertIntent.putExtra("message2", alert2)
        alertIntent.putExtra("message3", alert3)

        // Allows you to schedule for your application to do something at a later date
        // even if it is in he background or isn't active
        val alarmManager = ctxx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(ctxx, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))

    }
    override fun onPostExecute(result: Int?) {
        //showDialog("Downloaded " + result + " bytes");
    }
}