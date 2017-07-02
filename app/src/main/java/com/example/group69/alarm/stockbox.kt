package com.example.group69.alarm

data class Stock (val stockid: Long = 1337,
                  val ticker: String = "BABA",
                  var target: Double = 4.20,
                  var above: Long = 1,
                  var phone: Long = 0)