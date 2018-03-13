import org.junit.Test;
import com.advent.group69.tradetracker.Utility.toDollar
import com.advent.group69.tradetracker.Utility.validTickerSymbol
import org.amshove.kluent.shouldEqual

class Utilitytest {

    @Test
    fun shouldFormatDollarsCorrectly() {
        toDollar("2.0") shouldEqual "$2.00"
    }

    @Test
    fun shouldCheckValidTickerFormat() {
        validTickerSymbol("JELK90#$") shouldEqual false
        validTickerSymbol("1") shouldEqual false
        validTickerSymbol("0") shouldEqual false
        validTickerSymbol("R") shouldEqual false
        validTickerSymbol("25.36") shouldEqual false
        validTickerSymbol("1.0") shouldEqual false
        validTickerSymbol("GOOG") shouldEqual true
        validTickerSymbol("NYSE:C") shouldEqual true //with exchange code NYSE
        validTickerSymbol("GOOG.BY") shouldEqual true //with exchange code BY
        validTickerSymbol("$90") shouldEqual false
        validTickerSymbol("98774") shouldEqual true //because more than 4 digit long
        validTickerSymbol("789.BY") shouldEqual true //because ends with .[A-Z]{2,2}
    }

}