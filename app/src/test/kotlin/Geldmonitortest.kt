import org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import java.io.StringReader
import java.io.BufferedReader
import com.example.group69.alarm.Geldmonitor
import org.amshove.kluent.shouldBePositive
import org.amshove.kluent.shouldBeNegative
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual

/**
 * Unit test.
 */
class Geldmonitortest {

    @Test
    fun testParsing () {
        val ETH_USD_20181213 = reader("{\"USD\":846.31}")
        Geldmonitor.parseCryptoPrice(ETH_USD_20181213).shouldEqual(846.31)

        val GOOG_20181213 = reader("<td>\n<span id=\"quotes_content_left__LastSale\" style=\"display:inline-block;border-style:None;\">1053</span>")
        Geldmonitor.parseLiveStockPrice(GOOG_20181213).shouldEqual(1053.0)

        val MSFT_20181213 = reader("<div id=\"qwidget_lastsale\" class=\"qwidget-dollar\">\$89.83</div>\n")
        Geldmonitor.parseLateStockPrice(MSFT_20181213).shouldEqual(89.83)
    }

    @Test
    fun testNetworkAndParsing() {
        Geldmonitor.getCryptoPrice("ETH").shouldBePositive()
        Geldmonitor.getStockPrice("GOOG").shouldBePositive()
        Geldmonitor.getLateStockPrice("MSFT").shouldBePositive()
    }

    private fun reader(s: String): BufferedReader { return BufferedReader(StringReader(s)) }
}