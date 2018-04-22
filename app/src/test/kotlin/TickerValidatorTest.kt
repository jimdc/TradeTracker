import org.junit.Test
import com.advent.tradetracker.TickerValidator
import org.amshove.kluent.shouldEqual

/**
 * Spoiler alert: has the same problems as the Kotlin version
 */

class TickerValidatorTest {
    @Test
    fun shouldCheckValidTickerFormat() {
        val tickerValidator = TickerValidator()

        tickerValidator.validate("JELK90#$") shouldEqual false
        tickerValidator.validate("1") shouldEqual false
        tickerValidator.validate("0") shouldEqual false
        tickerValidator.validate("R") shouldEqual false
        tickerValidator.validate("25.36") shouldEqual false
        tickerValidator.validate("1.0") shouldEqual false
        tickerValidator.validate("GOOG") shouldEqual true
        tickerValidator.validate("NYSE:C") shouldEqual true //with exchange code NYSE
        tickerValidator.validate("GOOG.BY") shouldEqual true //with exchange code BY
        tickerValidator.validate("$90") shouldEqual false
        tickerValidator.validate("98774") shouldEqual true //because more than 4 digit long
        tickerValidator.validate("789.BY") shouldEqual true //because ends with .[A-Z]{2,2}
    }
}