import com.advent.tradetracker.StockDownloader
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldBePositive
import org.junit.Test

class AlphaVantageAPITest {

    /**
    results should be like:

     * {"USD":65.59}
     *
     * { "Meta Data":
     *   { "1. Information": "Batch Stock Market Quotes",
     *     "2. Notes": "IEX Real-Time Price provided for free by IEX (https://iextrading.com/developer/).",
     *     "3. Time Zone": "US/Eastern" },
     *
     *     "Stock Quotes": [
    {"1. symbol": "MSFT", "2. price": "96.2400", "3. volume": "12126968", "4. timestamp": "2018-05-17 16:00:00"},
    {"1. symbol": "FB", "2. price": "183.7200", "3. volume": "10879171", "4. timestamp": "2018-05-17 16:00:00"},
    {"1. symbol": "AAPL", "2. price": "187.0000", "3. volume": "14302750", "4. timestamp": "2018-05-17 16:00:00"}
    ]
     *
     */

    @Test
    fun stockQuotesAreSane() {
        val stockPrices = StockDownloader.getStockPricesAV("MSFT,FB,AAPL")
        stockPrices.forEach {
            it -> it shouldBeGreaterThan 0.0
        }
    }

    /**
    results should be like:

     *  {
    "Realtime Currency Exchange Rate": {
    "1. From_Currency Code": "BTC",
    "2. From_Currency Name": "Bitcoin",
    "3. To_Currency Code": "CNY",
    "4. To_Currency Name": "Chinese Yuan",
    "5. Exchange Rate": "52250.28570460",
    "6. Last Refreshed": "2018-05-17 21:03:15",
    "7. Time Zone": "UTC"
    }
    }
     */

    @Test
    fun cryptoQuoteIsSane() {
        val cryptoPrice = StockDownloader.getCryptoPriceAV("BTC")
        cryptoPrice.shouldBePositive()
    }

}