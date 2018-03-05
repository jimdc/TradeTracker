import android.support.test.rule.ServiceTestRule
import org.junit.Rule
import android.os.IBinder
import android.support.test.InstrumentationRegistry
import android.content.Intent
import org.junit.Test
import com.example.group69.alarm.MainService
import org.junit.Assert.assertThat
import java.util.concurrent.TimeoutException
import android.content.*
import android.os.Handler
import android.os.Message
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
/*
@RunWith(AndroidJUnit4::class)
public class MainServicetest : ServiceTestRule() {
    @Rule public val mServiceRule = ServiceTestRule()

    fun setUp() {
        val context = InstrumentationRegistry.getTargetContext()
        val serviceIntent = Intent(context, MainService::class.java)
        val binder = mServiceRule.bindService(serviceIntent)
    }

    @Test
    @Throws(TimeoutException::class)
    fun testWithBoundService() {

    }

}
        */