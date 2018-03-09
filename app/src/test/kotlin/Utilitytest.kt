import org.junit.Test;
import com.advent.group69.tradetracker.Utility.toDollar
import org.amshove.kluent.shouldEqual

class Utilitytest {

    @Test
    fun shouldFormatDollarsCorrectly() {
        toDollar("2.0") shouldEqual "$2.00"
    }

}