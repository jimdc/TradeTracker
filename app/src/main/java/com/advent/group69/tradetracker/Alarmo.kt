package com.advent.group69.tradetracker

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

/**
 * Not in use yet. For db version 3.
 */

@Entity(tableName = "alarmstable",
        foreignKeys = arrayOf(
                ForeignKey(entity=Stoko::class, parentColumns = arrayOf("stockid"),
                childColumns=arrayOf("myStockId"),
                onDelete= ForeignKey.CASCADE)
        )
)

data class Alarmo(
        @PrimaryKey @ColumnInfo(name="my_stock_id") val myStockId: Long = 0) {
    @ColumnInfo(name="target") var target: Double = 0.0
    @ColumnInfo(name="above") var above: Boolean = false
    @ColumnInfo(name="phone") var phone: Boolean = false
    @ColumnInfo(name="alarm_type") var alarmType: Int = 0
}