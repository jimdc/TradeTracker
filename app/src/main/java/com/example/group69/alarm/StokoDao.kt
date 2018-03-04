package com.example.group69.alarm

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import io.reactivex.Flowable

@Dao
interface StokoDao {
    @Query("SELECT * FROM stockstable")
    fun getAllStokos(): Flowable<List<Stoko>>

    @Query("SELECT * FROM stockstable WHERE stockId = :p0")
    fun findStokoById(id: Long): Stoko

    @Insert(onConflict = REPLACE)
    fun insert(stoko: Stoko)

    @Update(onConflict = REPLACE)
    fun update(stoko: Stoko)

    @Delete
    fun delete(stoko: Stoko)
}