<<<<<<< HEAD
import com.advent.tradetracker.StockDownloader
=======
import com.advent.tradetracker.StockDownloader.getCryptoPrice
import com.advent.tradetracker.StockDownloader.getStockPrice
import com.advent.tradetracker.StockDownloader.parseCryptoPrice
import com.advent.tradetracker.StockDownloader.parseLateStockPrice
import org.amshove.kluent.shouldBePositive
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.junit.Before
>>>>>>> fd7659fdcbce0757b4bf585fcb1690a6c5608a6d
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.BufferedReader
<<<<<<< HEAD
import com.advent.tradetracker.StockDownloader.parseLiveStockPrice
import com.advent.tradetracker.StockDownloader.parseLateStockPrice
import com.advent.tradetracker.StockDownloader.parseCryptoPrice
import com.advent.tradetracker.StockDownloader.getStockPrice
import com.advent.tradetracker.StockDownloader.getLateStockPrice
import com.advent.tradetracker.StockDownloader.getCryptoPrice
import org.amshove.kluent.*
=======
import java.io.StringReader
>>>>>>> fd7659fdcbce0757b4bf585fcb1690a6c5608a6d

/**
 * old hardcoded info: notice MSFT is "late" stock price mode in NASDAQ, GOOG is "live" stock price.
 * const val ETH_USD_20181213 = "{\"USD\":846.31}"
 * const val MSFT_20181213 = "<div id=\"qwidget_lastsale\" class=\"qwidget-dollar\">\$89.83</div>\n"
 * const val GOOG_20181213 = "<td>\n<span id=\"quotes_content_left__LastSale\" style=\"display:inline-block;border-style:None;\">1053</span>"
 *
 * some old tests which we don't have an equivalent for with these parameters
 * *sanity check for prices: expensive stock is more than cheaper stock
 * *confirming that some stock which is not a crypto isn't intepreted as such; likewise for crypto which is not stock ticker
**/

@RunWith(Parameterized::class)
class StockDownloaderTest (
        private val sharedTickerSymbol: String,
        private val unparsedAsCrypto: String,
        private val parsedAsCrypto: Double,
        private val unparsedAsStock: String,
        private val parsedAsStock: Double
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters

        fun data() = listOf(
                arrayOf("ETH", //Ethereum and Ethan Allen Interiors on 2018-03-12
                        "{\"USD\":463.7}", 463.7,
                        "<div id=\"qwidget_lastsale\" class=\"qwidget-dollar\">\$22.3</div>", 22.3
                        ),
                arrayOf("LTC", //Litecoin and LTC Properties, Inc. on 2018-03-12
                        "{\"USD\":120.95}", 120.95,
                        "<div id=\"qwidget_lastsale\" class=\"qwidget-dollar\">\$36.64</div>", 36.64
                        ),
                arrayOf("NEO", //NEO currency and NeoGeonomics on 2018-03-12
                        "{\"USD\":61.17}", 61.17,
                        "<div id=\"qwidget_lastsale\" class=\"qwidget-dollar\">\$8.32</div>", 8.32
                        )
            )
    }

    @Test fun parsesCryptoOutput () {
        parseCryptoPrice(unparsedAsCrypto).shouldEqual(parsedAsCrypto)
    }

    /**
     * parse"Live"StockPrice will not work because the format is "Late"
     */
    @Test fun parsesStockOutput () {
        parseLateStockPrice(asBufferedReader(unparsedAsStock)).shouldEqual(parsedAsStock)
    }

<<<<<<< HEAD
    @Test
    fun testTickerMismatch() {
        getStockPrice("ETH").shouldNotBe(com.advent.tradetracker.StockDownloader.getCryptoPrice("ETH")) //Ethan Allen Interiors != Ethereum
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
        val salliemae = com.advent.tradetracker.StockDownloader.getStockPrice("SLM")
        goog.shouldBeGreaterThan(salliemae)
=======
    private var asStockPrice = 0.0
    private var asCryptoPrice = 0.0

    @Before fun initialize() { //So we don't waste time with redundant network calls
        asStockPrice = getStockPrice(sharedTickerSymbol)
        asCryptoPrice = getCryptoPrice(sharedTickerSymbol)
    }

    @Test fun recognizesBothAsStockAndAsCrypto() {
        asStockPrice.shouldBePositive()
        asCryptoPrice.shouldBePositive()
>>>>>>> fd7659fdcbce0757b4bf585fcb1690a6c5608a6d
    }

    @Test fun stockAndCryptoPriceAreDistinct() {
        asStockPrice.shouldNotEqual(asCryptoPrice)
    }

    private fun asBufferedReader(htmlResult: String): BufferedReader {
        return BufferedReader(
                StringReader(htmlResult)
        )
    }
}