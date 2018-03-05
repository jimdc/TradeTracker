import com.example.group69.alarm.Geldmonitor
import org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import java.io.StringReader
import java.io.BufferedReader
import com.example.group69.alarm.Geldmonitor.parseLiveStockPrice
import com.example.group69.alarm.Geldmonitor.parseLateStockPrice
import com.example.group69.alarm.Geldmonitor.parseCryptoPrice
import com.example.group69.alarm.Geldmonitor.getStockPrice
import com.example.group69.alarm.Geldmonitor.getLateStockPrice
import com.example.group69.alarm.Geldmonitor.getCryptoPrice
import org.amshove.kluent.*

/**
 * Unit test.
 */
class Geldmonitortest {

    @Test
    fun testParsing () {
        val ETH_USD_20181213 = reader("{\"USD\":846.31}")
        parseCryptoPrice(ETH_USD_20181213).shouldEqual(846.31)
        //parseCryptoPrice looks for any number so it would parse the below incorrectly...

        val GOOG_20181213 = reader("<td>\n<span id=\"quotes_content_left__LastSale\" style=\"display:inline-block;border-style:None;\">1053</span>")
        parseLiveStockPrice(GOOG_20181213).shouldEqual(1053.0)
        parseLiveStockPrice(ETH_USD_20181213).shouldBeNegative()

        val MSFT_20181213 = reader("<div id=\"qwidget_lastsale\" class=\"qwidget-dollar\">\$89.83</div>\n")
        parseLateStockPrice(MSFT_20181213).shouldEqual(89.83)
        parseLateStockPrice(ETH_USD_20181213).shouldBeNegative()
    }

    @Test
    fun testTickerMismatch() {
        //Stock-wise, ETH is Ethan Allen Interiors, while ETH in Crypto is Ethereum
        getStockPrice("ETH").shouldNotBe(Geldmonitor.getCryptoPrice("ETH"))

        //BTC is a cryptocurrency but not a stock ticker so this should give an error.
        getStockPrice("BTC").shouldBeNegative()

        //MSFT is a stock but not a cryptocurrency so this should give an error
        getCryptoPrice("MSFT").shouldBeNegative()
    }

    @Test
    fun testNetworkAndParsing() {
        //Stock prices should be positive
        getCryptoPrice("ETH").shouldBePositive()
        val goog = getStockPrice("GOOG")
        goog.shouldBePositive()
        getLateStockPrice("MSFT").shouldBePositive()

        //Sanity check: more expensive vs cheaper stocks
        val salliemae = Geldmonitor.getStockPrice("SLM")
        goog.shouldBeGreaterThan(salliemae)
    }

    private fun reader(s: String): BufferedReader { return BufferedReader(StringReader(s)) }
}