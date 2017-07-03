package com.example.group69.alarm

data class Stock (val stockid: Long = 1337,
                  val ticker: String = "BABA",
                  var target: Double = 4.20,
                  var above: Long = 1,
                  var phone: Long = 0)
{
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
//Failed attempt at using ad-hoc polymorphism below.
/*{
    companion object {
        public var inst : Stock? = null

        operator fun invoke(stockid: Long = 1337,
                            ticker: String = "BABA",
                            target: Double = 4.20,
                            aboveB: Boolean = true,
                            phoneB: Boolean = false) = {
            var longAbove: Long = 1; if (!aboveB) { longAbove = 0; }
            var longPhone: Long = 0; if (phoneB) { longPhone = 0; }

            inst = Stock(stockid, ticker, target, longAbove, longPhone)
        }
    }
}*/