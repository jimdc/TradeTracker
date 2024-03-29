package com.advent.tradetracker.model

import android.content.Context
import android.database.sqlite.SQLiteException
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

/**
 * Database stuff done here. Not necessarily on its own thread...
 */

class DatabaseFunctions(val context: Context) : StockInterface {
    private var stockDatabase = StockDatabase.getInstance(context)
    private var stockDao = stockDatabase?.stockDao()

    override fun getCompositeDisposable() = CompositeDisposable()
    override fun getFlowingStockList(): Flowable<List<Stock>> = Flowable.empty()

    @Synchronized
    override fun deleteStockByStockId(stockId: Long) : Boolean {
        var rez: Int? = 0

        val stock = stockDao?.findStockById(stockId)

        if (stock != null) {
            try {
                rez = stockDao?.delete(stock)
            } catch (e: SQLiteException) {
                Timber.e( "could not delete $stockId: " + e.toString())
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
            Timber.e( "getStockList exception: " + e.toString())
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
    override fun addOrEditStock(stock: Stock): Boolean {

        if (stockDao == null) {
            Timber.d("stockDao is null; I cannot add or edit")
            return false
        } else {
            val rownum: Long? = stockDao!!.insert(stock)
            if (rownum == null || rownum == -1L) {
                Timber.d( "SQL function for add/edit failed.")
                return false
            } else {
                Timber.v( "Looks like that addition was a success.")
            }
        }

        return true
    }

    fun cleanup() {
        StockDatabase.destroyInstance()
    }

    fun deleteStockByPosition(position: Int) : Boolean {
        val stocks: List<Stock> = getStockList()
        val stockid = stocks[position].stockid
        return deleteStockByStockId(stockid)
    }
}