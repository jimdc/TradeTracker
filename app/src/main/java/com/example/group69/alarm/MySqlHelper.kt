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
        db.createTable("Portfolio", true,
                "_stockid" to INTEGER + PRIMARY_KEY,
                "ticker" to TEXT,
                "urgency" to TEXT,
                "volatility" to INTEGER,
                "targetprice" to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

}

// Access property for Context
val Context.database: MySqlHelper
    get() = MySqlHelper.getInstance(applicationContext)