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
    fun getAllStokosFlowable(): Flowable<List<Stoko>>

    @Query("SELECT * FROM stockstable")
    fun getAllStokos(): List<Stoko>

    //@Query("SELECT * FROM Alarmo WHERE my_stock_id =: p0")
    //fun getAlarmoList(stockId: Long)

    @Query("SELECT * FROM stockstable WHERE stock_id = :id")
    fun findStokoById(id: Long): Stoko

    @Insert(onConflict = REPLACE)
    fun insertStoko(stoko: Stoko)

    //@Insert
    //fun insertAlarmos(alarmos: List<Alarmo>)

    @Update(onConflict = REPLACE)
    fun update(stoko: Stoko)

    @Delete
    fun delete(stoko: Stoko)
}