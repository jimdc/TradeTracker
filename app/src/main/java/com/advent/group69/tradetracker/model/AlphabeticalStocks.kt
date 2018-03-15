package com.advent.group69.tradetracker.model

class AlphabeticalStocks {
    companion object : Comparator<Stock> {
        override fun compare(a: Stock, b: Stock): Int = a.ticker.compareTo(b.ticker)
    }
}