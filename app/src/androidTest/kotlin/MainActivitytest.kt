import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.group69.alarm.MainActivity
import com.example.group69.alarm.R
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.click
import android.support.v7.widget.Toolbar
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import android.content.Intent
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.*
import android.widget.ToggleButton
import com.example.group69.alarm.scanRunning


/**
 * Instrumentation test. If get "Empty test suite", right click run from package
 * RuntimeException because needs to have getActivity() startActivitySync or similar called
 */

@Rule
public val mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

@RunWith(AndroidJUnit4::class)
class MainActivitytest {

    lateinit var activity: MainActivity

    @Before
    fun setup() {
        val intent = Intent()
        activity = mActivityRule.launchActivity(intent)
    }

    @Test
    fun ensureScanningIndicatorResponsiveness() {
        //Ensure that it's not scanning on startup.
        onView(withId(R.id.show_scanning_switch)).check(matches(withText(R.string.notscanning)))
        //Ensure that it shows a different message when you do click.
        onView(withId(R.id.show_scanning_switch)).perform(click()) //To turn it on...
        onView(withId(R.id.show_scanning_switch)).check(matches(withText(R.string.scanning)));
        onView(withId(R.id.show_scanning_switch)).perform(click()) //And to turn it off.
        onView(withId(R.id.show_scanning_switch)).check(matches(withText(R.string.notscanning)));
    }

    @Test
    fun ensureToolbarIsPresent() {
        val cooltool = activity.findViewById(R.id.cooltoolbar) as Toolbar
        assertThat(cooltool, instanceOf(Toolbar::class.java))
    }

    @Test
    fun addStock() {
        onView(withId(R.id.addstockorcrypto)).perform(click())
        onView(withText(R.string.addstock)).perform(click())
    }

}