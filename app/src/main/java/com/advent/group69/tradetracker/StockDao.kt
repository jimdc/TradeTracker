package com.advent.group69.tradetracker

import android.arch.persistence.room.*
import io.reactivex.Flowable

@Dao
interface StockDao {
    //See for Maybe, Single, Flowable: https://medium.com/google-developers/room-rxjava-acb0cd4f3757

    @Query("SELECT * FROM TableView2")
    fun getAllStocksF(): Flowable<List<Stock>>

    @Query("SELECT * FROM TableView2")
    fun getAllStocks(): List<Stock>

    @Query("SELECT * FROM TableView2 WHERE _stockid = :id")
    fun findStockById(id: Long): Stock

    //Insert works fine to update but not vice-versa for some reason.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stock: Stock): Long

    @Delete
    fun delete(stock: Stock): Int

    @Query("DELETE FROM TableView2")
    fun nukeall()
}