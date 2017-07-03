package com.example.group69.alarm

data class Stock (val stockid: Long = 1337,
                  val ticker: String = "BABA",
                  var target: Double = 4.20,
                  var above: Long = 1,
                  var phone: Long = 0)
{
    constructor(_stockid: Long = 1337, _ticker: String = "BABA", _target: Double = 4.20, aboveB: Boolean = true, phoneB: Boolean = false) :
    this (_stockid, _ticker, _target,
            when(aboveB) { true -> {1L} false -> {0L} },
            when(phoneB) { true -> {1L} false -> {0L} }
        ) {
    }

    fun ContentValues() : android.content.ContentValues {
        val con = android.content.ContentValues()
        con.put("_stockid", this.stockid)
        con.put("ticker", this.ticker)
        con.put("target", this.target)
        con.put("ab", this.above)
        con.put("phone", this.phone)
        return con
    }
}