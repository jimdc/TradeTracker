package com.advent.tradetracker.future

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import io.reactivex.Flowable

/**
 * Not in use yet. For db version 3.
 */

@Dao
interface StokoDao {
    @Query("SELECT * FROM stockstable")
    fun getAllStokosFlowable(): Flowable<List<com.advent.tradetracker.future.Stoko>>

    @Query("SELECT * FROM stockstable")
    fun getAllStokos(): List<com.advent.tradetracker.future.Stoko>

    //@Query("SELECT * FROM Alarmo WHERE my_stock_id =: p0")
    //fun getAlarmoList(stockId: Long)

    @Query("SELECT * FROM stockstable WHERE stock_id = :id")
    fun findStokoById(id: Long): com.advent.tradetracker.future.Stoko

    @Insert(onConflict = REPLACE)
    fun insertStoko(stoko: com.advent.tradetracker.future.Stoko)

    //@Insert
    //fun insertAlarmos(alarmos: List<Alarmo>)

    @Update(onConflict = REPLACE)
    fun update(stoko: com.advent.tradetracker.future.Stoko)

    @Delete
    fun delete(stoko: com.advent.tradetracker.future.Stoko)
}