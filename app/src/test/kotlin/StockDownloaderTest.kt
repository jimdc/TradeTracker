import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.BufferedReader
import com.advent.tradetracker.StockDownloader.getStockPrice
import com.advent.tradetracker.StockDownloader.getCryptoPrice
import org.amshove.kluent.*
import org.junit.Before
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
        private val sharedTickerSymbol: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters

        fun data() = listOf(
                arrayOf("ETH") //Ethereum and Ethan Allen Interiors
                ,arrayOf("LTC") //Litecoin and LTC Properties, Inc
                ,arrayOf("NEO") //NEO currency and NeoGeonomics
            )
    }

    private var asStockPrice = 0.0
    private var asCryptoPrice = 0.0

    @Before
    fun initialize() { //So we don't waste time with redundant network calls
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