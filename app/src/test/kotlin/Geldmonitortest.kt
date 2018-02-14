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

    /**
     * It doesn't work yet because it needs a network connection.
     * (Simulating that is a bit complicated.)
     * If Geldmonitor does android.Log.d, that needs to be mocked,
     * so I commented out all instances of logging on Geldmonitor.
     */
    @Test
    fun shouldReturnSomething() {
        val ETH_USD_20181213 = reader("{\"USD\":846.31}")
        Geldmonitor.parseCryptoPrice(ETH_USD_20181213).shouldEqual(846.31)

        Geldmonitor.getCryptoPrice("ETH").shouldBePositive()
        Geldmonitor.getStockPrice("GOOG").shouldBePositive() //wrongly returns -1.0
    }

    private fun reader(s: String): BufferedReader { return BufferedReader(StringReader(s)) }
}