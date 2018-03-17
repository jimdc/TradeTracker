package com.advent.group69.tradetracker.model

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable

interface StockInterface {
    fun getCompositeDisposable(): CompositeDisposable
    fun getFlowingStockList(): Flowable<List<Stock>>
    fun addOrEditStock(stock: Stock): Boolean
    fun deleteStockByStockId(stockId: Long) : Boolean
}