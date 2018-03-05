package com.example.group69.alarm

import android.content.Intent
import android.os.IBinder
import android.os.Binder
import android.app.Service
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import java.util.Random

//Apparently the Datenbank?.use { } was causing IllegalStateException by closing it.

class DatabaseService : Service() {
    // Binder given to clients
    private val mBinder = LocalBinder()
    // Random number generator
    private val mGenerator = Random()

    /** method for clients  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    var stockDatabase: StockDatabase? = null

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal// Return this instance of DatabaseService so clients can call public methods
        val service: DatabaseService
            get() = this@DatabaseService
    }

    override fun onBind(intent: Intent): IBinder {
        stockDatabase = StockDatabase.getInstance(this.applicationContext)
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
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

        val stock = stockDatabase?.stockDao()?.findStockById(stockid)
        try {
            if (stock != null) rez = stockDatabase?.stockDao()?.delete(stock)
        } catch (e: SQLiteException) {
            Log.e("DatabaseService", "could not delete $stockid: " + e.toString())
        }

        if (rez == null || rez!! <= 0) return false

        mModel.stocks.postValue(getStocklistFromDB()) //Synchronize
        return true
    }

    @Synchronized
    fun getStocklistFromDB() : List<Stock> {
        var results: List<Stock>? = null

        try {
            results = stockDatabase?.stockDao()?.getAllStocks()
        } catch (e: SQLiteException) {
            Log.e("DatabaseService", "getStocklistFromDB exception: " + e.toString())
        }

        return results ?: emptyList()
    }

    @Synchronized
    fun addeditstock(stock: Stock): Boolean {

        var rownum: Long? = 666L
        rownum = stockDatabase?.stockDao()?.insert(stock)

        if (rownum == 666L || rownum == -1L || rownum == null) {
            Log.d("DatabaseService", "That was a fail.")
            return false
        }

        mModel.stocks.postValue(getStocklistFromDB()) //Synchronize
        return true
    }
}