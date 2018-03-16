import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.advent.group69.tradetracker.viewmodel.MainActivity
import com.advent.group69.tradetracker.R
import org.junit.runner.RunWith
import android.content.Context
import org.junit.*

@Rule @JvmField
val muhActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

@RunWith(AndroidJUnit4::class)
class Snoozetest {

    private lateinit var activity: MainActivity
    private lateinit var context: Context

    @Before
    fun setup() {
        val intent = Intent()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        activity = muhActivityRule.launchActivity(intent)
    }

    @Test
    fun continuesScanningAfterSnoozing() {
        onView(withId(R.id.show_scanning_switch)).check(matches(ViewMatchers.withText(R.string.notscanning)))
        onView(withId(R.id.show_scanning_switch)).perform(click())
        onView(withId(R.id.show_scanning_switch)).check(matches(ViewMatchers.withText(R.string.scanning)))

        //Set it to openSnoozeDialog for 1 second.
        onView(withId(R.id.snoozeSetTimeTextview)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.inputHour)).perform(typeText("0"))
        onView(withId(R.id.inputMinute)).perform(typeText("1"))
        onView(withId(R.id.btnSnooze)).perform(click())
        onView(withId(R.id.show_scanning_switch)).check(matches(ViewMatchers.withText(R.string.notscanning)))
        Thread.sleep(1000)

        onView(withId(R.id.show_scanning_switch)).check(matches(ViewMatchers.withText(R.string.scanning)))
        onView(withId(R.id.show_scanning_switch)).perform(click())
    }

    @Test
    fun trysnoozingIncorrectly() {
        openActionBarOverflowOrOptionsMenu(context)
        onView(ViewMatchers.withText(R.string.snooze)).perform(ViewActions.click())
        onView(withId(R.id.snoozeSetTimeTextview)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.btnSnooze)).perform(click()) //I didn't enter any number
    }

    @After
    fun teardown() {
    }

}