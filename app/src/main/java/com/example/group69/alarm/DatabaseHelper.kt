package com.example.group69.alarm

import org.jetbrains.anko.db.*
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlin.Number
import java.util.concurrent.atomic.AtomicInteger


val NewestDatabaseName = "NewCryptoDB"
val NewestTableName = "TableView2"

class DatabaseHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, NewestDatabaseName) {
    private val mDatabase: SQLiteDatabase? = null
    private val mInstance: DatabaseHelper? = null
    private val mContext: Context? = null

    companion object {
        private var instance: DatabaseHelper? = null
        val dbVersion = 1

        @Synchronized
        fun getInstance(ctx: Context): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(ctx.applicationContext)
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

    /**
     * @todo implement for [SQLiteSingletontest]
     */
    fun clearDbAndRecreate() { }

    /**
     * @todo implement for [SQLiteSingletontest]
     */
    fun clearDb() { }
}

// Access property for Context
val Context.database: DatabaseHelper
    get() = DatabaseHelper.getInstance(applicationContext)
