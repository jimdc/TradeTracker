package com.advent.group69.tradetracker.future

import android.arch.persistence.room.*
import com.advent.group69.tradetracker.future.Alarmo

@Dao
interface AlarmoDao {
    @Insert
    fun insert (alarmo: Alarmo)

    @Update
    fun update (alarmo: Alarmo)

    @Delete
    fun delete(alarmo: Alarmo)
}