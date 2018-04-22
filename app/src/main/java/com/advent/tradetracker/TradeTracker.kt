package com.advent.tradetracker

import android.app.Application
import com.advent.tradetracker.model.StockRestService

val stockRestService by lazy { StockRestService.create() }

class TradeTracker : Application()