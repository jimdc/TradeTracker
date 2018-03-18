import android.support.test.rule.ServiceTestRule
import android.support.test.InstrumentationRegistry
import android.content.Intent
import com.advent.group69.tradetracker.NetworkService
import java.util.concurrent.TimeoutException
import android.support.test.runner.AndroidJUnit4
import org.junit.*
import org.junit.runner.RunWith

@Rule
val serviceRule = ServiceTestRule()

@Ignore
@RunWith(AndroidJUnit4::class)
class MainServicetest {

    @Before
    @Test(timeout=1000 * 60)
    fun setUp() {
        val context = InstrumentationRegistry.getTargetContext()
        val serviceIntent = Intent(context, NetworkService::class.java)
        try {
            val service = serviceRule.startService(serviceIntent)
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testUnboundService() {

    }

}