package com.advent.tradetracker.future

import android.arch.persistence.room.*

@Dao
interface AlarmoDao {
    @Insert
    fun insert (alarmo: com.advent.tradetracker.future.Alarmo)

    @Update
    fun update (alarmo: com.advent.tradetracker.future.Alarmo)

    @Delete
    fun delete(alarmo: com.advent.tradetracker.future.Alarmo)
}