import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.espresso.Espresso
import android.support.v7.widget.Toolbar
import java.lang.annotation.Annotation
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import android.content.Intent
import android.support.annotation.IdRes
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.*
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.v7.widget.RecyclerView
import android.widget.ToggleButton
import com.example.group69.alarm.*


/**
 * Instrumentation test. If get "Empty test suite", right click run from package
 * RuntimeException because needs to have getActivity() startActivitySync or similar called
 */

@Rule @JvmField
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
        //Ensure toolbar is present.
        val cooltool = activity.findViewById(R.id.cooltoolbar) as Toolbar
        assertThat(cooltool, instanceOf(Toolbar::class.java))

        //Ensure that it's not scanning on startup.
        onView(withId(R.id.show_scanning_switch)).check(matches(withText(R.string.notscanning)))
        //Ensure that it shows a different message when you do click.
        onView(withId(R.id.show_scanning_switch)).perform(click()) //To turn it on...
        onView(withId(R.id.show_scanning_switch)).check(matches(withText(R.string.scanning)))
        onView(withId(R.id.show_scanning_switch)).perform(click()) //And to turn it off.
        onView(withId(R.id.show_scanning_switch)).check(matches(withText(R.string.notscanning)))
    }

    @Test
    fun addStockAndThenEditItAndThenDeleteIt() {
        Intents.init()
        val intelstock = Stock(stockid = 78901, ticker = "INTC", target = 60.5, above = 1L, phone = 0L, crypto = 0L)
        val amdstockname = "AMD"

        onView(withId(R.id.addstockorcrypto)).perform(click())
        onView(withText(R.string.addstock)).perform(click())

        intended(hasComponent(AddEditStockActivity::class.java.name)) //verify AddEditStockActivity has opened

        onView(withId(R.id.tickerName)).check(matches(isDisplayed()))
        //If you don' close the soft keyboard, it throws espresso.InjectEventSecurityException: java.lang.SecurityException
        onView(withId(R.id.tickerName)).perform(typeText(intelstock.ticker), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.tickerPrice)).perform(typeText(intelstock.target.toString()), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.rbAbove)).perform(click())
        onView(withId(R.id.fab)).perform(click())

        onView(withText(intelstock.toString())).check(matches(isDisplayed()))

        onView(withId(R.id.recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0, //The first item in the list. @todo change it to something more sustainable
                        MyViewAction.clickChildViewWithId(R.id.imgEditStock)))

        //checking that the AddEditActivity intent has open here will match 2 intents instead of one.
        onView(withId(R.id.tickerName)).perform(replaceText(amdstockname), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.fab)).perform(click())

        onView(withId(R.id.recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0, //The first item in the list. @todo change it to something more sustainable
                        MyViewAction.clickChildViewWithId(R.id.imgDeleteStock)))

        onView(withText(R.string.yes)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click()) //button1 is "Yes", or positive

        Intents.release()
    }

}