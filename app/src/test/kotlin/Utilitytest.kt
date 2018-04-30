import org.junit.Test
import com.advent.tradetracker.Utility.withDollarSignAndDecimal
import com.advent.tradetracker.Utility.isValidTickerSymbol
import org.amshove.kluent.shouldEqual

class UtilityTest {

    @Test
    fun shouldFormatDollarsCorrectly() {
        "2.0".withDollarSignAndDecimal() shouldEqual "$2.00"
    }

    @Test
    fun shouldCheckValidTickerFormat() {
        "JELK90#$".isValidTickerSymbol() shouldEqual false
        "1".isValidTickerSymbol() shouldEqual false
        "0".isValidTickerSymbol() shouldEqual false
        "R".isValidTickerSymbol() shouldEqual true
        "25.36".isValidTickerSymbol() shouldEqual false
        "1.0".isValidTickerSymbol() shouldEqual false
        "GOOG".isValidTickerSymbol() shouldEqual true
        "NYSE:C".isValidTickerSymbol() shouldEqual true //with exchange code NYSE
        "GOOG.BY".isValidTickerSymbol() shouldEqual true //with exchange code BY
        "$90".isValidTickerSymbol() shouldEqual false
        "98774".isValidTickerSymbol() shouldEqual true //because more than 4 digit long
        "789.BY".isValidTickerSymbol() shouldEqual true //because ends with .[A-Z]{2,2}
    }

}