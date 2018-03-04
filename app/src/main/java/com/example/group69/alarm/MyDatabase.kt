package com.example.group69.alarm

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(Stoko::class), version=1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun stokoDao(): StokoDao
}