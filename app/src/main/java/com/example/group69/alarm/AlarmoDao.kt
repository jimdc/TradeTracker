package com.example.group69.alarm

import android.arch.persistence.room.*

@Dao
public interface AlarmoDao {
    @Insert
    fun insert (alarmo: Alarmo)

    @Update
    fun update (alarmo: Alarmo)

    @Delete
    fun delete(alarmo: Alarmo)
}