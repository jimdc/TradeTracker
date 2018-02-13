import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.group69.alarm.MainActivity
import com.example.group69.alarm.R
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.action.ViewActions.click
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4

/**
 * Instrumentation test. "Empty test suite" for now, something wrong with Gradle config
 */
class AddStocktest {

    private val mStringToBeTyped: String = "Espresso"
    public val mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    public fun clickclack() {
        onView(withId(R.id.addstockButton)).perform(click())
    }


}