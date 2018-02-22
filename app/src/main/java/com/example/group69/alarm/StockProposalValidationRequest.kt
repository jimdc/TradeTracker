package com.example.group69.alarm

import android.content.Context;
import android.util.Log
import android.widget.Toast

/**
 * This class would asynchronously check if a stock really exists before doing an edit.
 * @see it used in the onclick listener in [AddEditStockActivity]
 * @todo Check if the stock really exists before doing the edit
 */
class StockProposalValidationRequest(ctx: Context) : android.os.AsyncTask<Stock, Int, CharSequence>() {
    //Parameters are: param, progress, result

    val Datenbank = DatabaseManager.getInstance().database
    private val context: Context = ctx

    override fun doInBackground(vararg stocks: Stock): CharSequence? {

        Log.d("DIB", "starting doInBackground")

        for (stock in stocks) {
            Log.d("DIB", "doInBackground for stock " + stock.ticker)

            val target: Double? = stock.target
            if (target == null) {
                return context.getResources().getString(R.string.NaN, "NaN") //It's a Double, so... fix later
            }

            var rownum: Long? = 666
            Datenbank?.use {
                rownum = Datenbank?.replace(NewestTableName, null, stock.ContentValues())
            }

            var result = context.getResources().getString(R.string.fail2edit)
            if (rownum != -1L) {
                //Previously there was an "addsuccess" distinction but not in the asynchronous rewrite
                result = context.getResources().getString(R.string.editsuccess) +
                        "#${rownum}: " + stock.toString()
            }

            DatabaseManager.getInstance().database.close()
            return result
        }

        return null
    }

    /**
     * @todo use broadcast receiver stuff, not just toast
     */
    override fun onPostExecute(result: CharSequence?) {
        var toastmsg = "Some exotic error happened."
        if (result != null) {
            toastmsg = result.toString()
        }
        val toast = Toast.makeText(context, toastmsg, Toast.LENGTH_SHORT)
        toast.show()
    }

}