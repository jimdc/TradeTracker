package com.example.group69.alarm

import android.arch.persistence.room.*
import android.arch.persistence.room.Entity
import java.time.Instant

/**
 * Not in use yet. For db version 3.
 */

@Entity(tableName = "stockstable")
data class Stoko(var ticker: String = "") {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="stock_id") var stockId: Long = 0

    @ColumnInfo(name="market_cap") var marketCap: Long = 0
    @ColumnInfo(name="avg_volume") var avgVolume: Long = 0
    @ColumnInfo(name="todays_volume") var todaysVolume: Long = 0
    @ColumnInfo(name="shares_outstanding") var sharesOutstanding: Int = 0
    @ColumnInfo(name="crypto") var crypto: Boolean = false
    @ColumnInfo(name="num_alarms") var numAlarms: Int = 0
    @ColumnInfo(name="last_price") var lastPrice: Long = 0
    @ColumnInfo(name="last_price_as_of")
    var lastPriceAsOf: Long = 0

    @Embedded var stokoPosition = StokoPosition()

    @Ignore private var alarmos: List<Alarmo>? = null
    @Ignore @ColumnInfo(name="current_price") var currentprice: Long = 0

    override fun toString() = ticker
}

data class StokoPosition(
        @ColumnInfo(name="num_shares") var numShares: Int = 0,
        @ColumnInfo(name="price_shares") var priceShares: Long = 0)