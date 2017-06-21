package com.example.group69.alarm

/**
 * Created by james on 17/06/2017.
 */

object SyntaxAnalysierer {

    // Given symbol, get HTML
    private fun readHTML(symbol: String): String {
        try {
            val doc = org.jsoup.Jsoup.connect("https://finance.yahoo.com/quote/$symbol?ltr=1").timeout(0).get()
            return doc.html()
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error"
        }

    }

    fun PriceAndPercent(symbol: String): String {
        val hitmill = readHTML(symbol)
        if (!hitmill.equals("Error")) {
            val price = priceOf(hitmill, symbol)
            val percent = Prozent(hitmill, symbol)

            val combined = price.toString() + "(" + percent.toString() + "%)"
            return combined
        } else {
            return "ER!"
        }
    }

    fun priceOf(symbol: String): Double {
        return priceOf(readHTML(symbol), symbol)
    }

    // Given symbol, get current stock price.
    private fun priceOf(hitmill: String, symbol: String): Double {
        val price: String
        val rez: String
        val p: Int
        val from: Int
        val to: Int

        //p = html.indexOf("Fz(36px) Mb(-4px)", 0);      // "yfs_l84" index
        p = hitmill.indexOf("Mb(-4px) D(ib)", 0)
        if (p != -1) {
            from = hitmill.indexOf(">", p)            // ">" index
            if (from != -1) {
                to = hitmill.indexOf("</span>", from)   // "</span>" indexs
                if (to != -1) {
                    price = hitmill.substring(from + 1, to)
                    rez = price.replace(",".toRegex(), "")
                    try {
                        return java.lang.Double.parseDouble(rez)
                    } catch (e: java.lang.NumberFormatException) {
                        return 4.20;
                    }
                } else {
                    return -1.0 //could not find "to"
                }
            } else {
                return -2.0 //could not find "from"
            }
        } else {
            return -3.0 //could not find "p"
        }

    }

    fun Prozent(symbol: String): Double {
        return Prozent(readHTML(symbol), symbol)
    }

    private fun Prozent(hitmill: String, symbol: String): Double {
        val identifier = "Trsdu(0.3s) Fw(500) Pstart(10px) Fz(24px) C(\$dataRed)"
        val startPct = hitmill.indexOf(identifier)
        if (startPct != -1) {
            val from = hitmill.indexOf("(", startPct + identifier.length) + 1
            val to = hitmill.indexOf("%", from)
            if (from != -1 && to != -1) {
                val percentage = hitmill.substring(from, to)
                return java.lang.Double.parseDouble(percentage)
            } else {
                return -2.0
            }
        } else {
            return -3.0
        }
    }
}

