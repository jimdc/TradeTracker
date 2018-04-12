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
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Unit test.
 */
const val ETH_USD_20181213 = "{\"USD\":846.31}"
const val MSFT_20181213 = "<div id=\"qwidget_lastsale\" class=\"qwidget-dollar\">\$89.83</div>\n"
const val GOOG_20181213 = "<td>\n<span id=\"quotes_content_left__LastSale\" style=\"display:inline-block;border-style:None;\">1053</span>"

@RunWith(Parameterized::class)
class StockDownloaderTest (val unparsed: String, val parsed: Double) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
                arrayOf("{\"USD\":846.31}", 846.31), //Ethereum on December 12, 2018 (???, maybe February)
                arrayOf("{\"USD\":7655.98}", 7655.98) //Bitcoin on March 12, 2018
            )
    }

    private val readEthereum = reader(ETH_USD_20181213)
    private val readMicrosoft = reader(MSFT_20181213)
    private val readGoogle = reader(GOOG_20181213)

    @Test
    fun testParsesCryptoJSON () {
        parseCryptoPrice(unparsed).shouldEqual(parsed)
    }

    @Ignore("Not parameterized yet.")
    @Test
    fun testParsingHappyPath () {
        parseCryptoPrice(ETH_USD_20181213).shouldEqual(846.31)
        parseLiveStockPrice(readGoogle).shouldEqual(1053.0)
        parseLateStockPrice(readMicrosoft).shouldEqual(89.83)
    }

    @Ignore("Not parameterized yet.")
    @Test
    fun testParsingErrors() {
        parseCryptoPrice(MSFT_20181213).shouldBeNegative()
        parseLiveStockPrice(readEthereum).shouldBeNegative()
        parseLateStockPrice(readEthereum).shouldBeNegative()
    }

    @Ignore("Not parameterized yet.")
    @Test
    fun testTickerMismatch() {
        getStockPrice("ETH").shouldNotBe(StockDownloader.getCryptoPrice("ETH")) //Ethan Allen Interiors != Ethereum
        getStockPrice("BTC").shouldBeNegative() //BTC is a cryptocurrency but not a stock ticker
        getCryptoPrice("MSFT").shouldBeNegative() //MSFT is a stock but not a cryptocurrency
    }

    @Ignore("Not parameterized yet.")
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