package com.example.group69.alarm


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



class Updaten(ctx: Context) : android.os.AsyncTask<String, String, Int>() {
    var stocksTargets : List<Stock> = ArrayList()
    var stocksNow: MutableList<StockNow> = mutableListOf<StockNow>()
    var result: Double? = null
    var ctxx = ctx
    var i = 0
    var alarmPlayed : Boolean = false
    override fun doInBackground(vararg tickers: String): Int? {

        while(true){

            var manager: MySqlHelper = MySqlHelper.getInstance(this.ctxx)
            val database = manager.writableDatabase
            //val sqlDB = MySqlHelper(ctxx)

            Log.d("Attempting Updaten", "Updaten")
            try {
                database.use {
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
                val stock = yahoofinance.YahooFinance.get(stockx.ticker.toString()); //this will keep checking duplicates with
                //the yahoo api when it could save time by using the saved value for that stock in a table
                var currPrice : Double = stock.quote.price.toDouble()
                if(stockx.above == 1L){
                    if(currPrice >= stockx.target && !alarmPlayed){ //will need to DELETE THE ALARMPLAYED
                        alarmPlayed = true;
                        publishProgress("result", a.toString()) //return index so we know which stock to remove from database
                    }
                }
                else{
                    if(currPrice <= stockx.target && !alarmPlayed){
                        alarmPlayed = true;
                        publishProgress("result", a.toString()) //return index so we know which stock to remove from database
                    }
                }
               // val change = stock.quote.changeInPercent

               // publishProgress(ticker, price.toString(), change.toString())
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
        //Log.d("Stock", progress[0] + " price: " + progress[1] + "pctchange" + progress[2])
        val intent = Intent("com.example.group69.alarm")
        intent.putExtra(progress[0],progress[1])
        LocalBroadcastManager.getInstance(this.ctxx).sendBroadcast(intent)
    }

    inner class MyUndoListener : View.OnClickListener {

        override fun onClick(v: View) {

            // Code to undo the user's last action
        }
    }

    override fun onPostExecute(result: Int?) {
        //showDialog("Downloaded " + result + " bytes");
    }
}