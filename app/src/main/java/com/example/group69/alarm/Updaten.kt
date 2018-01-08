package com.example.group69.alarm


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
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
import android.os.SystemClock
import android.os.Vibrator
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

        TimeZone.getTimeZone("EST")
        var failcount = 0
        var timeCount: Long = 0
        val date = Date()
        GregorianCalendar().time
        val cal = Calendar.getInstance()
        cal.time = date
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        Log.d("hours", hours.toString())
        //while(!Thread.currentThread().isInterrupted()){
        var ww = 0
         while(true) {
             if (isCancelled()) {
                 break
             }
            Log.d("updaten","iteration #" + ww)
            ww++
             var manager: MySqlHelper = MySqlHelper.getInstance(this.ctxx)
            val database = manager.writableDatabase
            //val sqlDB = MySqlHelper(ctxx)

            Log.d("Attempting Updaten", "Up")
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
                Log.d("In Updaten: ", "sqlLite error onCreate: " + e.toString())
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
                var currPrice = -2.0
                try {
                    //currPrice = stock.quote.price.toDouble()
                    currPrice = Geldmonitor.getPrice(ticker)
                    Log.d("Errorlog", "got the symb: " + ticker)
                    Log.d("Errorlog", "got the price: " + currPrice)
                } catch (e: Exception){
                    currPrice = -3.0
                    //TODO
                    //when returning the stock's current price to update the gui (which will only happen when gui is opened or remains opened)
                    //we will show the current price of the stock (updates minimum every 10 seconds, more than that will drain battery)
                    //if we get back -3.0 as the error code, we will display 'failed to obtain price, alert paused',
                    // if ALL stocks in stock list have this problem, but cryptos work: (or vise-versa): we will let user know that there is an error
                    // connecting to the stock/crypto exchange server
                    // if both are not working simultaneously: we will display that there is no data connection/wifi found, scanning will resume
                    // automatically when connection is found.

                    //for now just checking to see if all stocks are returning -3.0
                    Log.d("Errorlog", "got stock " + stockx.ticker.toString() + " caused NPE!")
                }

                if (currPrice >= 0) {
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
                else{ //if price is less than 0, we have an error from network, might be one stock. if it is all we will find out with:
                    failcount++
                }
                a++
            }
            if(failcount == stocksTargets.size){
                //TODO this is where broadcaster will send the mainActivity to
                // update stock prices as ERROR and give a single notification about network connection being lost.
                //we dont want to spam network connection lost every minute!!!
                Log.d("got","network connection error, all stocks getting -3.0")
                try {
                 Thread.sleep(60000)
                } catch (ie: InterruptedException) {
                 ie.printStackTrace()
                 Thread.currentThread().interrupt()
                }

            }
            failcount = 0
            //publishProgress("alert")
            timeCount++
            /*if(timeCount == 1L || timeCount % 30 == 0L){
                Log.d("timeCount",timeCount.toString() + "time:" + Date().hours.toString())
                if(Date().hours !in 7..16){
                    Thread.sleep(28 * 60000)
                }
                Log.d("sleep","done sleeping")
            }
            */
            try {
                Thread.sleep(8000)
            } catch (ie: InterruptedException) {
                ie.printStackTrace()
                Thread.currentThread().interrupt()
            }


        }
        Log.d("Errorlog","got thread interupted")
        return 0
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
    fun pause(time: Long){
        SystemClock.sleep(time * 60000);

        //Thread.sleep(time * 60000)
    }
    fun playAlarm(ticker: String, price: String, ab: String) {

        Log.d("playAlarm","" + ab)
        // Define a time value of 5 seconds
        val alertTime = GregorianCalendar().timeInMillis + 5
        var gain: String
        // Define our intention of executing AlertReceiver
        if(ab == "1"){
            gain = "rose to"
        }
        else gain = "dropped to"

        val alertIntent = Intent(ctxx, AlertReceiver::class.java)
        val alert1 : String
        alert1 = ticker.toUpperCase() + " " + gain + " " + toDollar(price)
        val alert2 : String
        alert2 = toDollar(price)
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

        val v = ctxx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val num : LongArray = longArrayOf(0,1500,1500,1500,1500,1500,1500,1500,1500,1500,1500,1500,1500)
        v.vibrate(num, -1)


    }
    override fun onPostExecute(result: Int?) {
        //showDialog("Downloaded " + result + " bytes");
    }
    fun toDollar(d: String): String{
        var s = d
        var dot = s.indexOf('.')
        if(dot + 1 == s.length - 1) {
            s = s + '0'
        }
        var sub = s.substring(0,dot)

        var str = StringBuilder(sub)
        if(sub.length>3){

            str.insert(sub.length-3,',')
        }



        return '$' + str.toString() + s.substring(dot,s.length)
    }

}