import com.advent.group69.tradetracker.StockDownloader
import org.junit.Test
import java.io.StringReader
import java.io.BufferedReader
import com.advent.group69.tradetracker.StockDownloader.parseLiveStockPrice
import com.advent.group69.tradetracker.StockDownloader.parseLateStockPrice
import com.advent.group69.tradetracker.StockDownloader.parseCryptoPrice
import com.advent.group69.tradetracker.StockDownloader.getStockPrice
import com.advent.group69.tradetracker.StockDownloader.getLateStockPrice
import com.advent.group69.tradetracker.StockDownloader.getCryptoPrice
import org.amshove.kluent.*

/**
 * Unit test.
 */
const val ETH_USD_20181213 = "{\"USD\":846.31}"
const val MSFT_20181213 = "<div id=\"qwidget_lastsale\" class=\"qwidget-dollar\">\$89.83</div>\n"
const val GOOG_20181213 = "<td>\n<span id=\"quotes_content_left__LastSale\" style=\"display:inline-block;border-style:None;\">1053</span>"

class Geldmonitortest {
    val readEthereum = reader(ETH_USD_20181213)
    val readMicrosoft = reader(MSFT_20181213)
    val readGoogle = reader(GOOG_20181213)

    @Test
    fun testParsingHappyPath () {
        parseCryptoPrice(ETH_USD_20181213).shouldEqual(846.31)
        parseLiveStockPrice(readGoogle).shouldEqual(1053.0)
        parseLateStockPrice(readMicrosoft).shouldEqual(89.83)
    }

    @Test
    fun testParsingErrors() {
        parseCryptoPrice(MSFT_20181213).shouldBeNegative()
        parseLiveStockPrice(readEthereum).shouldBeNegative()
        parseLateStockPrice(readEthereum).shouldBeNegative()
    }

    @Test
    fun testTickerMismatch() {
        getStockPrice("ETH").shouldNotBe(StockDownloader.getCryptoPrice("ETH")) //Ethan Allen Interiors != Ethereum
        getStockPrice("BTC").shouldBeNegative() //BTC is a cryptocurrency but not a stock ticker
        getCryptoPrice("MSFT").shouldBeNegative() //MSFT is a stock but not a cryptocurrency
    }

    @Test
    fun testNetworkAndParsing() {
        //Stock prices should be positive
        getCryptoPrice("ETH").shouldBePositive()
        val goog = getStockPrice("GOOG")
        goog.shouldBePositive()
        getLateStockPrice("MSFT").shouldBePositive()

        //Sanity check: more expensive vs cheaper stocks
        val salliemae = StockDownloader.getStockPrice("SLM")
        goog.shouldBeGreaterThan(salliemae)
    }

    private fun reader(s: String): BufferedReader { return BufferedReader(StringReader(s)) }
}