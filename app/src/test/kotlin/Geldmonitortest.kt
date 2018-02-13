import org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import com.example.group69.alarm.Geldmonitor
import org.amshove.kluent.shouldBePositive

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
        val GOOGprice = Geldmonitor.getStockPrice("GOOG")
        val ETHprice = Geldmonitor.getCryptoPrice("ETH")

        GOOGprice.shouldBePositive() //returns -1.0
        ETHprice.shouldBePositive() //returns -3.0
    }

}