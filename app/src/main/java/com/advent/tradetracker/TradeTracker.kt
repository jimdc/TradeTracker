package com.advent.tradetracker

import android.app.Application
import com.advent.tradetracker.model.StockRestService
import timber.log.Timber;
import timber.log.Timber.DebugTree



val stockRestService by lazy { StockRestService.create() }

class TradeTracker : Application() {
    override fun onCreate() {
        super.onCreate()

        //It is recommended to separate out this logic by build instead of programmatically
        //https://medium.com/@caueferreira/timber-enhancing-your-logging-experience-330e8af97341
        //But didn't do this yet because our project structure doesn't match that in the tutorial

        if (BuildConfig.DEBUG)
            Timber.plant(DebugTree())
        else
            Timber.plant(ReleaseTree())
    }
}