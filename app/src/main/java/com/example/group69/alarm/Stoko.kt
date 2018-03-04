package com.example.group69.alarm

import android.arch.persistence.room.*
import android.arch.persistence.room.Entity
import java.time.Instant

@Entity(tableName = "stockstable")
data class Stoko(val ticker: String = "") {
    @PrimaryKey(autoGenerate = true)
    val stockId: Long = 0

    val marketCap: Long = 0
    val avgVolume: Long = 0
    val todaysVolume: Long = 0
    val sharesOutstanding: Int = 0
    val crypto: Boolean = false
    val numAlarms: Int = 0
    val lastPrice: Long = 0
    val lastPriceAsOf: Long = 0

    @Embedded
    val stokoPosition = StokoPosition()

    @Ignore
    val currentprice: Long = 0
}

data class StokoPosition(val numShares: Int = 0, val priceShares: Long = 0)

@Entity(foreignKeys = arrayOf(ForeignKey(entity=Stoko::class, parentColumns = arrayOf("stockid"),
        childColumns=arrayOf("myStockId"), onDelete=ForeignKey.CASCADE)))

data class Alarmo(val myStockId: Long = 0) {
    val target: Double = 0.0
    val above: Boolean = false
    val phone: Boolean = false
    val alarmType: Int = 0
}