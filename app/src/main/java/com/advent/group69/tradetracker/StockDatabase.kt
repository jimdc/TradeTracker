package com.advent.group69.tradetracker

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(Stock::class), version=2, exportSchema = false)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao

    companion object {
        private var INSTANCE: StockDatabase? = null

        public fun getInstance(context: Context): StockDatabase? {
            if (INSTANCE == null) {
                synchronized(StockDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            StockDatabase::class.java, "NewCryptoDB")
                            .allowMainThreadQueries() //@todo. Not do this. It's terrible etc
                            .fallbackToDestructiveMigration() //@todo. Also terrible.
                            .build()
                }
            }
            return INSTANCE
        }

        public fun destroyInstance() { INSTANCE = null }
    }
}