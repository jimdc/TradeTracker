package com.advent.tradetracker.model

import android.os.Parcelable
import android.arch.persistence.room.*
import kotlinx.android.parcel.Parcelize

/**
 * @param[activationPrice] becomes -2.0 to signify that the activation price has been met
 */

@Parcelize
@Entity(tableName="TableView2")
data class Stock(@PrimaryKey @ColumnInfo(name="_stockid") var stockid: Long = 1337,
                 var ticker: String = "BABA",
                 var target: Double = -1.0,
                 var stopLoss: Double = -1.0, //for trailing
                 var trailingPercent: Double = -1.0,
                 var activationPrice: Double = -1.0,
                 var highestPrice: Double = -1.0,
                 @ColumnInfo(name="ab") var above: Long = 1,
                 var phone: Long = 0,
                 var crypto: Long = 0) : Parcelable