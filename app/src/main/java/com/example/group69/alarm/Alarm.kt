package com.example.group69.alarm

import android.app.Application
import android.arch.persistence.room.Room
import android.util.Log

class Alarm : Application() {
    companion object {
        var database: StokoDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()
        Alarm.database = Room.databaseBuilder(this, StokoDatabase::class.java, "wee-need-db").build()
        Log.d("Did this work", "Hi")
    }
}