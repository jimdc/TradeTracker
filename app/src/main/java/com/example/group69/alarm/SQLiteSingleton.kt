package com.example.group69.alarm

import org.jetbrains.anko.db.*
import android.content.Context
import android.database.sqlite.SQLiteDatabase

val NewestDatabaseName = "NewCryptoDB"
val NewestTableName = "TableView2"

/**
 * Anko class to avoid having to manually open, close, try, catch.
 * Enables worry-free use of <code>Datenbank.use { }</code>
 */
class SQLiteSingleton(ctx: Context) : ManagedSQLiteOpenHelper(ctx, NewestDatabaseName) {
    private val mDatabase: SQLiteDatabase? = null
    private val mInstance: SQLiteSingleton? = null
    private val mContext: Context? = null

    companion object {
        private var instance: SQLiteSingleton? = null
        val dbVersion = 1

        @Synchronized
        fun getInstance(ctx: Context): SQLiteSingleton {
            if (instance == null) {
                instance = SQLiteSingleton(ctx.applicationContext)
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
    fun clearDbAndRecreate() {

    }

    /**
     * @todo implement for [SQLiteSingletontest]
     */
    fun clearDb() {

    }
}

// Access property for Context
val Context.database: SQLiteSingleton
    get() = SQLiteSingleton.getInstance(applicationContext)

