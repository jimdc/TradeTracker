package com.example.group69.alarm

import org.jetbrains.anko.db.*
import android.content.Context
import android.database.sqlite.SQLiteDatabase

val NewestDatabaseName = "NewCryptoDB"
val NewestTableName = "TableView2"

class MySqlHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, NewestDatabaseName) {
    private val mDatabase: SQLiteDatabase? = null
    private val mInstance: MySqlHelper? = null
    private val mContext: Context? = null

    companion object {
        private var instance: MySqlHelper? = null
        val dbVersion = 1

        @Synchronized
        fun getInstance(ctx: Context): MySqlHelper {
            if (instance == null) {
                instance = MySqlHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(NewestTableName, true,
                "_stockid" to INTEGER + PRIMARY_KEY + UNIQUE,
                "ticker" to TEXT,
                "target" to REAL,
                "ab" to INTEGER,
                "phone" to INTEGER,
                "crypto" to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(NewestTableName, true)
        onCreate(db)
    }

}

// Access property for Context
val Context.database: MySqlHelper
    get() = MySqlHelper.getInstance(applicationContext)

