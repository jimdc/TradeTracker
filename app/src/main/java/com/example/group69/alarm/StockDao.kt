package com.example.group69.alarm

import android.arch.persistence.room.*
import io.reactivex.Flowable

@Dao
interface StockDao {
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