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

class DatabaseServiceRoom : Service() {
    // Binder given to clients
    private val mBinder = LocalBinder()

    var stokoDatabase: StokoDatabase? = null

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal// Return this instance of DatabaseSortaService so clients can call public methods
        val service: DatabaseServiceRoom
            get() = this@DatabaseServiceRoom
    }

    override fun onBind(intent: Intent): IBinder {
        stokoDatabase = StokoDatabase.getInstance(this.applicationContext)
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        StokoDatabase.destroyInstance()
    }

    fun deletestock(position: Int) {
        val stocks: List<Stoko> = getStocklistFromDB()
        val stockid = stocks[position].stockId
        deletestockInternal(stockid)
    }

    @Synchronized
    fun deletestockInternal(stockid: Long) {
        val stoko = stokoDatabase?.stokoDao()?.findStokoById(stockid)
        if (stoko != null) stokoDatabase?.stokoDao()?.delete(stoko)
        // UPDATE or DELETE queries can return void or int. If it is an int, the value is the number of rows affected by this query.
    }

    @Synchronized
    fun getStocklistFromDB(): List<Stoko> {
        val stokos = stokoDatabase?.stokoDao()?.getAllStokos()

        //Syntax is wrong but also concept is wrong...
        //Since this is Flowable, DatabaseServiceRoom should subscribe?

        //if (flowable == null) return emptyList()
        //else return flowable?.toList().
        return stokos ?: emptyList()
    }

    @Synchronized
    fun addstoko(stoko: Stoko) {
        stokoDatabase?.stokoDao()?.insertStoko(stoko)
    }

    @Synchronized
    fun updatestoko(stoko: Stoko) {
        stokoDatabase?.stokoDao()?.update(stoko)
    }
}
