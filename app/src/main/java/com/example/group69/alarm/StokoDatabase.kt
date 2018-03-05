package com.example.group69.alarm

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(Stoko::class), version=1, exportSchema = false)
abstract class StokoDatabase : RoomDatabase() {
    abstract fun stokoDao(): StokoDao

    companion object {
        private var INSTANCE: StokoDatabase? = null

        public fun getInstance(context: Context): StokoDatabase? {
            if (INSTANCE == null) {
                synchronized(StokoDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            StokoDatabase::class.java, "stoko.db").build()
                }
            }
            return INSTANCE
        }

        public fun destroyInstance() { INSTANCE = null }
    }
}