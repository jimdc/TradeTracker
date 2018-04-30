package com.advent.tradetracker

object Utility {
    fun String.withDollarSignAndDecimal(): String {
        var s = this
        val dot = s.indexOf('.')

        if (dot + 1 == s.length - 1) {
            s += '0'
        }

        val sub = s.substring(0, dot)
        val str = StringBuilder(sub)

        if (sub.length > 3) {
            str.insert(sub.length - 3, ',')
        }

        return '$' + str.toString() + s.substring(dot, s.length)
    }

    /**
     * Reduces repetitive try-catch blocks for sleep.
     * Prints stack trace and interrupts thread
     */
    fun sleepWithThreadInterruptIfWokenUp(milliseconds: Long) {
        try {
            Thread.sleep(milliseconds)
        } catch (ie: InterruptedException) {
            ie.printStackTrace()
            Thread.currentThread().interrupt()
        }
    }

    /**
     *
     */
    fun String.isValidTickerSymbol(): Boolean {
        val validTickerFormat =
                Regex("^(([a-z]{2,4}):(?![a-z\\d]+\\.))?([a-z]{1,4}|\\d{1,3}(?=\\.)|\\d{4,})(\\.([a-z]{2}))?$",
                        RegexOption.IGNORE_CASE)

        val results = validTickerFormat.matchEntire(this)?.groupValues

        return (results != null)
    }
}