package com.advent.group69.tradetracker.future

import android.arch.persistence.room.*

@Dao
interface AlarmoDao {
    @Insert
    fun insert (alarmo: Alarmo)

    @Update
    fun update (alarmo: Alarmo)

    @Delete
    fun delete(alarmo: Alarmo)
}