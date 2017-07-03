package com.example.group69.alarm

import android.util.Log
import android.view.View

internal class Updaten : android.os.AsyncTask<String, String, Int>() {

    var result: Double? = null

    override fun doInBackground(vararg tickers: String): Int? {
        while (true) {
            for (ticker in tickers) {
                val stock = yahoofinance.YahooFinance.get(ticker);
                val price = stock.quote.price
                val change = stock.quote.changeInPercent

                publishProgress(ticker, price.toString(), change.toString())
            }

            try {
                Thread.sleep(2000)
            } catch (ie: InterruptedException) {
                ie.printStackTrace()
                Thread.currentThread().interrupt()
            }
        }
    }

    override fun onProgressUpdate(vararg progress: String) {
        Log.d("Stock", progress[0] + " price: " + progress[1] + "pctchange" + progress[2])
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