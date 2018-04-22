package com.advent.tradetracker

import android.app.Application
import com.advent.tradetracker.model.StockRestService
import timber.log.Timber
import timber.log.Timber.DebugTree

val stockRestService by lazy { StockRestService.create() }