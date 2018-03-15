package com.advent.group69.tradetracker.model

interface StockInterface {
    fun addOrEditStock(stock: Stock): Boolean
    fun deleteStockByStockId(stockId: Long) : Boolean
}