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

    val ETH_USD_20181213 = "{\"USD\":846.31}"
    val rETH_USD_20181213 = reader(ETH_USD_20181213)
    val MSFT_20181213 = "<div id=\"qwidget_lastsale\" class=\"qwidget-dollar\">\$89.83</div>\n"
    val rMSFT_20181213 = reader(MSFT_20181213)
    val GOOG_20181213 = "<td>\n<span id=\"quotes_content_left__LastSale\" style=\"display:inline-block;border-style:None;\">1053</span>"
    val rGOOG_20181213 = reader(GOOG_20181213)

    @Test
    fun testParsingHappyPath () {
        parseCryptoPrice(ETH_USD_20181213).shouldEqual(846.31)
        parseLiveStockPrice(rGOOG_20181213).shouldEqual(1053.0)
        parseLateStockPrice(rMSFT_20181213).shouldEqual(89.83)
    }

    @Test
    fun testParsingErrors() {
        parseCryptoPrice(MSFT_20181213).shouldBeNegative()
        parseLiveStockPrice(rETH_USD_20181213).shouldBeNegative()
        parseLateStockPrice(rETH_USD_20181213).shouldBeNegative()
    }

    @Test
    fun testTickerMismatch() {
        getStockPrice("ETH").shouldNotBe(Geldmonitor.getCryptoPrice("ETH")) //Ethan Allen Interiors != Ethereum
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
        val salliemae = Geldmonitor.getStockPrice("SLM")
        goog.shouldBeGreaterThan(salliemae)
    }

    private fun reader(s: String): BufferedReader { return BufferedReader(StringReader(s)) }
}