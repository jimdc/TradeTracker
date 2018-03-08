package com.example.group69.alarm

import android.content.Intent
import android.os.IBinder
import android.os.Binder
import android.app.Service
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import io.reactivex.Flowable
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import java.util.Random

/**
 * Database stuff done here. Not necessarily on its own thread...
 */

class WrapperAroundDao(val context: Context) {
    var stockDatabase = StockDatabase.getInstance(context)
    var stockDao = stockDatabase?.stockDao()

    public fun cleanup() {
        StockDatabase.destroyInstance()
    }

    fun deletestock(position: Int) : Boolean {
        val stocks: List<Stock> = getStocklistFromDB()
        val stockid = stocks[position].stockid
        return deletestockInternal(stockid)
    }

    @Synchronized
    fun deletestockInternal(stockid: Long) : Boolean {
        var rez: Int? = 0

        val stock = stockDao?.findStockById(stockid)
        if (stock != null) {
            try {
                rez = stockDao?.delete(stock)
            } catch (e: SQLiteException) {
                Log.e("WrapperAroundDao", "could not delete $stockid: " + e.toString())
            }
        }

        if (rez == null || rez!! <= 0) {
            return false
        }

        return true
    }

    /**
     * Used by [Updaten], but really, that service should also subscribe to [getFlowableStocklist]
     */
    @Synchronized
    fun getStocklistFromDB() : List<Stock> {
        var results: List<Stock>? = null

        try {
            results = stockDatabase?.stockDao()?.getAllStocks()
        } catch (e: SQLiteException) {
            Log.e("WrapperAroundDao", "getStocklistFromDB exception: " + e.toString())
        }

        return results ?: emptyList()
    }

    /**
     * For the UI thread subscription
     */
    @Synchronized
    fun getFlowableStocklist() : Flowable<List<Stock>> {
        return stockDao!!.getAllStocksF()
    }

    @Synchronized
    fun addeditstock(stock: Stock): Boolean {

        var rownum: Long? = stockDao?.insert(stock)

        if (rownum == null || rownum == -1L) {
            Log.d("WrapperAroundDao", "That was a fail.")
            return false
        }

        return true
    }
}