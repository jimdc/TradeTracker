import android.support.test.rule.ServiceTestRule
import android.os.IBinder
import android.support.test.InstrumentationRegistry
import android.content.Intent
import com.example.group69.alarm.MainService
import org.junit.Assert.assertThat
import java.util.concurrent.TimeoutException
import android.content.*
import android.os.Handler
import android.os.Message
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import org.junit.*
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@Rule
public val mServiceRule = ServiceTestRule()

@Ignore
@RunWith(AndroidJUnit4::class)
public class MainServicetest {

    @Before
    @Test(timeout=1000 * 60)
    fun setUp() {
        val context = InstrumentationRegistry.getTargetContext()
        val serviceIntent = Intent(context, MainService::class.java)
        try {
            val service = mServiceRule.startService(serviceIntent)
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testUnboundService() {

    }

}