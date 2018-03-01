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

    var Datenbank: SQLiteDatabase? = null

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
        DatabaseManager.initializeInstance(DatabaseHelper(this.applicationContext))
        Datenbank = DatabaseManager.getInstance().database

        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        DatabaseManager.getInstance().database.close()
    }


    @Synchronized
    fun deletestockInternal(stockid: Long) : Boolean {
        var rez: Int? = 0

        try {
            rez = Datenbank?.delete(NewestTableName, "_stockid=$stockid")
        } catch (e: SQLiteException) {
            Log.e("MainActivity", "could not delete $stockid: " + e.toString())
        }

        if (rez!! > 0) {
            mModel.stocks.postValue(getStocklistFromDB()) //Synchronize
            return true
        }

        return false
    }

    @Synchronized
    fun getStocklistFromDB() : List<Stock> {
        var results: List<Stock> = ArrayList()
        try {
            val sresult = Datenbank?.select(NewestTableName, "_stockid", "ticker", "target", "ab", "phone", "crypto")

            sresult?.exec {
                if (this.count > 0) {
                    val parser = rowParser { stockid: Long, ticker: String, target: Double, above: Long, phone: Long, crypto: Long ->
                        Stock(stockid, ticker, target, above, phone, crypto)
                    }
                    results = parseList(parser)
                }
            }
        } catch (e: SQLiteException) {
            Log.e("DatabaseService", "getStocklistFromDB exception: " + e.toString())
        }

        return results
    }

    @Synchronized
    fun addeditstock(stock: Stock): Boolean {
        val target: Double? = stock.target
        var rownum: Long? = 666
        rownum = Datenbank?.replace(NewestTableName, null, stock.ContentValues())

        if (rownum == -1L) return false
        else {
            mModel.stocks.postValue(getStocklistFromDB()) //Synchronize
            return true
        }
    }
}