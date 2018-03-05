package com.example.group69.alarm

import android.arch.persistence.room.*
import io.reactivex.Flowable
import org.jetbrains.annotations.NotNull

@Dao
interface StockDao {
    //See for Maybe, Single, Flowable: https://medium.com/google-developers/room-rxjava-acb0cd4f3757

    @Query("SELECT * FROM TableView2")
    fun getAllStocksF(): Flowable<List<Stock>>

    @Query("SELECT * FROM TableView2")
    fun getAllStocks(): List<Stock>

    @Query("SELECT * FROM TableView2 WHERE _stockid = :id")
    fun findStockById(id: Long): Stock

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stock: Stock): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(stock: Stock): Int

    @Delete
    fun delete(stock: Stock): Int
}