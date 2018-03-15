package com.advent.group69.tradetracker.model

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import io.reactivex.Flowable

/**
 * Database stuff done here. Not necessarily on its own thread...
 */

class WrapperAroundDao(val context: Context) {
    var stockDatabase = StockDatabase.getInstance(context)
    var stockDao = stockDatabase?.stockDao()

    fun cleanup() {
        StockDatabase.destroyInstance()
    }

    fun deleteStockByPosition(position: Int) : Boolean {
        val stocks: List<Stock> = getStockList()
        val stockid = stocks[position].stockid
        return deleteStockByStockId(stockid)
    }

    @Synchronized
    fun deleteStockByStockId(stockid: Long) : Boolean {
        var rez: Int? = 0

        val stock = stockDao?.findStockById(stockid)



        if (stock != null) {
            try {
                rez = stockDao?.delete(stock)
            } catch (e: SQLiteException) {
                Log.e("WrapperAroundDao", "could not delete $stockid: " + e.toString())
            }
        }

        if (rez == null || rez <= 0) {
            return false
        }

        return true
    }

    /**
     * Used by Updaten, but really, that service should also subscribe to [getFlowableStockList]
     */
    @Synchronized
    fun getStockList() : List<Stock> {
        var results: List<Stock>? = null

        try {
            results = stockDatabase?.stockDao()?.getAllStocks()
        } catch (e: SQLiteException) {
            Log.e("WrapperAroundDao", "getStockList exception: " + e.toString())
        }

        return results ?: emptyList()
    }

    /**
     * For the UI thread subscription
     */
    @Synchronized
    fun getFlowableStockList() : Flowable<List<Stock>> {
        return stockDao!!.getFlowableStocks()
    }

    @Synchronized
    fun addOrEditStock(stock: Stock): Boolean {

        val rownum: Long? = stockDao?.insert(stock)

        if (rownum == null || rownum == -1L) {
            Log.d("WrapperAroundDao", "That was a fail.")
            return false
        }

        return true
    }
}