package com.example.group69.alarm

import android.os.Parcelable
import android.os.Parcel

data class Stock(val stockid: Long = 1337,
                 val ticker: String = "BABA",
                 var target: Double = 4.20,
                 var above: Long = 1,
                 var phone: Long = 0,
                 var crypto: Long = 0) : Parcelable {

    constructor(_stockid: Long = 1337, _ticker: String = "BABA", _target: Double = 4.20,
                aboveB: Boolean = true, phoneB: Boolean = false, crypto: Boolean = false) :
            this(_stockid, _ticker, _target,
                    when (aboveB) { true -> {1L} false -> {0L} },
                    when (phoneB) { true -> {1L} false -> {0L} },
                    when (crypto) { true -> {1L} false -> {0L} }
            ) {
    }

    fun ContentValues(): android.content.ContentValues {
        val con = android.content.ContentValues()
        con.put("_stockid", this.stockid)
        con.put("ticker", this.ticker)
        con.put("target", this.target)
        con.put("ab", this.above)
        con.put("phone", this.phone)
        con.put("crypto", this.crypto)
        return con
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readString(),
            source.readDouble(),
            source.readLong(),
            source.readLong(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(stockid)
        writeString(ticker)
        writeDouble(target)
        writeLong(above)
        writeLong(phone)
        writeLong(crypto)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Stock> = object : Parcelable.Creator<Stock> {
            override fun createFromParcel(source: Parcel): Stock = Stock(source)
            override fun newArray(size: Int): Array<Stock?> = arrayOfNulls(size)
        }
    }
    //The arrayOfNulls was generated by boilerplate. It might be better to have default values.
}