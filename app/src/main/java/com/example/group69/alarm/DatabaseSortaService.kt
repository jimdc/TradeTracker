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

class DatabaseSortaService(val context: Context) {
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
                Log.e("DatabaseSortaService", "could not delete $stockid: " + e.toString())
            }
        }

        if (rez == null || rez!! <= 0) {
            return false
        }

        return true
    }

    @Synchronized
    fun getStocklistFromDB() : List<Stock> {
        var results: List<Stock>? = null

        try {
            results = stockDatabase?.stockDao()?.getAllStocks()
        } catch (e: SQLiteException) {
            Log.e("DatabaseSortaService", "getStocklistFromDB exception: " + e.toString())
        }

        return results ?: emptyList()
    }

    @Synchronized
    fun getFlowableStocklist() : Flowable<List<Stock>> {
        return stockDao!!.getAllStocksF()
    }

    @Synchronized
    fun addeditstock(stock: Stock): Boolean {

        var rownum: Long? = stockDao?.insert(stock)

        if (rownum == null || rownum == -1L) {
            Log.d("DatabaseSortaService", "That was a fail.")
            return false
        }

        return true
    }
}