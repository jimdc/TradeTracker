package com.example.group69.alarm;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {

    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initialize(..) method first.");
        }

        return instance;
    }

    public synchronized SQLiteDatabase getDatabase() {
        return mDatabaseHelper.getWritableDatabase();
    }

}

/*package com.example.group69.alarm

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.concurrent.atomic.AtomicInteger

class DatabaseManager {
    private var mDatabase: SQLiteDatabase? = null
    private val mOpenCounter = AtomicInteger()
    @Synchronized
    fun openDatabase(): SQLiteDatabase? {
        if (mOpenCounter.incrementAndGet() === 1) {
            // Opening new database
            mDatabase = mDatabaseHelper!!.writableDatabase
        }
        return mDatabase
    }

    @Synchronized
    fun closeDatabase() {
        if (mOpenCounter.decrementAndGet() === 0) {
            // Closing database
            mDatabase!!.close()

        }
    }

    companion object {

        var instance: DatabaseManager? = null
            @Synchronized
            get() {
                if (instance == null) {
                    throw IllegalStateException(DatabaseManager::class.java.simpleName +
                            " is not initialized, call initializeInstance(..) method first.")
                }

                return instance
            }

        private var mDatabaseHelper: SQLiteOpenHelper? = null

        @Synchronized
        fun initializeInstance(helper: SQLiteOpenHelper) {
            if (instance == null) {
                instance = DatabaseManager()
                mDatabaseHelper = helper
            }
        }
    }
}*/
