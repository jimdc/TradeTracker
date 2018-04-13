package com.advent.group69.tradetracker

import android.app.Application
import com.advent.group69.tradetracker.model.StockRestService

val stockRestService by lazy { StockRestService.create() }

class TradeTracker : Application()