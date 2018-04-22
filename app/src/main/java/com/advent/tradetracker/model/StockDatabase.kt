package com.advent.tradetracker.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context


@Database(entities = [(Stock::class)], version=3, exportSchema = false)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao

    companion object {
        private var INSTANCE: StockDatabase? = null

        fun getInstance(context: Context): StockDatabase? {
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

        /*
        * for the future
        val MIGRATION_1_2: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE product " +
                        " ADD COLUMN price INTEGER")
            }
        }*/

        fun destroyInstance() { INSTANCE = null }
    }
}