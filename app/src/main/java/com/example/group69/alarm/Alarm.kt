package com.example.group69.alarm

import android.app.Application
import android.arch.persistence.room.Room

class Alarm : Application() {
    companion object {
        var database: MyDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()
        Alarm.database = Room.databaseBuilder(this, MyDatabase::class.java, "wee-need-db").build()
    }
}