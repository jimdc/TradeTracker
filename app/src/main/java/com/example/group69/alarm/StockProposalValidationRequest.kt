package com.example.group69.alarm

import android.content.Context;
import android.util.Log
import android.widget.Toast
import java.lang.ref.*

/**
 * Created by james on 8/28/17.
 */
class StockProposalValidationRequest(ctx: Context) : android.os.AsyncTask<Stock, Int, CharSequence>() {
    //Parameters are: param, progress, result

    private val context: Context = ctx

    override fun doInBackground(vararg stocks: Stock): CharSequence? {

        Log.d("DIB", "starting doInBackground")

        val manager: MySqlHelper = MySqlHelper.getInstance(context)
        val database = manager.writableDatabase

        for (stock in stocks) {
            Log.d("DIB", "doInBackground for stock " + stock.ticker)

            //Somehow check if the stock really exists. Used to do with Yahoo Finance app

            val target: Double? = stock.target
            if (target == null) {
                return context.getResources().getString(R.string.NaN, "NaN") //It's a Double, so... fix later
            }

            var rownum: Long = 666
            database.use {
                rownum = database.replace(NewestTableName, null, stock.ContentValues())
            }

            var result = context.getResources().getString(R.string.fail2edit)
            if (rownum != -1L) {
                //Previously there was an "addsuccess" distinction but not in the asynchronous rewrite
                result = context.getResources().getString(R.string.editsuccess) +
                        "#${rownum}: " + stock.toString()
            }

            return result
        }

        return null
    }

    override fun onPostExecute(result: CharSequence?) {
        var toastmsg = "Some exotic error happened."
        if (result != null) {
            toastmsg = result.toString()
        }

        //use broadcast receiver stuff
        val toast = Toast.makeText(context, toastmsg, Toast.LENGTH_SHORT)
        toast.show()
    }

}