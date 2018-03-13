package com.advent.group69.tradetracker

import android.os.Parcelable
import android.os.Parcel
import android.arch.persistence.room.*
import kotlinx.android.parcel.Parcelize

/**
 * @param[activationPrice] becomes -2.0 to signify that the activation price has been met
 */

@Parcelize
@Entity(tableName="TableView2")
data class Stock(@PrimaryKey @ColumnInfo(name="_stockid") var stockid: Long = 1337,
                 var ticker: String = "BABA",
                 var target: Double = 4.20,
                 var percent: Double = -1.0,
                 var stopLossPercent: Double = -1.0,
                 var activationPrice: Double = -1.0,
                 var highestPrice: Double = -1.0,
                 @ColumnInfo(name="ab") var above: Long = 1,
                 var phone: Long = 0,
                 var crypto: Long = 0) : Parcelable {

    /**
     * Overloaded constructor for better interoperability with UI radiobuttons
     * @param[aboveB] takes boolean and converts it to long equivalent
     * @param[phoneB] takes boolean and converts it to long equivalent
     * @param[crypto] takes boolean and converts it to long equivalent
     */
    constructor(_stockid: Long = 1337,
                _ticker: String = "BABA",
                _target: Double = 4.20,
                _percent: Double = -1.0,
                _stopLossPercent: Double = -1.0,
                _activationPrice: Double = -1.0,
                _highestPrice: Double = -1.0,
                aboveB: Boolean = true,
                phoneB: Boolean = false,
                crypto: Boolean = false) :
            this(_stockid, _ticker, _target, _percent, _stopLossPercent, _activationPrice, _highestPrice,
                    when (aboveB) { true -> {1L} false -> {0L} },
                    when (phoneB) { true -> {1L} false -> {0L} },
                    when (crypto) { true -> {1L} false -> {0L} }
            )
}