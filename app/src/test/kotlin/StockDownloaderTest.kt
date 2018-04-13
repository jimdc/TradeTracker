import com.advent.group69.tradetracker.StockDownloader.getCryptoPrice
import com.advent.group69.tradetracker.StockDownloader.getCryptoPriceObservable
import com.advent.group69.tradetracker.StockDownloader.getStockPrice
import com.advent.group69.tradetracker.StockDownloader.parseCryptoPrice
import com.advent.group69.tradetracker.StockDownloader.parseLateStockPrice
import org.amshove.kluent.shouldBePositive
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.BufferedReader
import java.io.StringReader

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

    @Test fun getsCryptoAsObservable() {
        val cryptoObservable = getCryptoPriceObservable(sharedTickerSymbol)
        cryptoObservable.blockingSingle().shouldBePositive()
    }

    private var asStockPrice = 0.0
    private var asCryptoPrice = 0.0

    @Before fun initialize() { //So we don't waste time with redundant network calls
        asStockPrice = getStockPrice(sharedTickerSymbol)
        asCryptoPrice = getCryptoPrice(sharedTickerSymbol)
    }

    @Test fun recognizesBothAsStockAndAsCrypto() {
        asStockPrice.shouldBePositive()
        asCryptoPrice.shouldBePositive()
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