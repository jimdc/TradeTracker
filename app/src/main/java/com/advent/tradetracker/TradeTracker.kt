package com.advent.tradetracker

import android.app.Application
import com.advent.tradetracker.model.AlphaVantageService
import com.advent.tradetracker.model.NASDAQService
import com.advent.tradetracker.model.StockRestService
import timber.log.Timber
import timber.log.Timber.DebugTree

val stockRestService by lazy { StockRestService.create() }
val nASDAQService by lazy { NASDAQService.create() }
val alphaVantageService by lazy { AlphaVantageService.create() }