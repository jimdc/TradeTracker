package com.example.group69.alarm

import org.jetbrains.anko.db.*;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

class MySqlHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "mydb") {

    companion object {
        private var instance: MySqlHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MySqlHelper {
            if (instance == null) {
                instance = MySqlHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db?.createTable("Portfolio", true,
                "_stockid" to INTEGER + PRIMARY_KEY,
                "ticker" to TEXT,
                "urgency" to TEXT,
                "volatility" to INTEGER,
                "targetprice" to INTEGER)
    }

    /*fun insert(stock: Stock) {
        db.insert("Portfolio",
                "_stockid" to 42,
                "ticker" to stock.name,
                "urgency" to stock.urgency,
                "volatility" to stock.volatility,
                "targetprice" to stock.targetprice
        )
    }*/

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db?.dropTable("User", true)
    }

}

// Access property for Context
val Context.database: MySqlHelper
    get() = MySqlHelper.getInstance(applicationContext)