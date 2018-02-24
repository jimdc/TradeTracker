import com.example.group69.alarm.DatabaseService
import android.os.IBinder
import android.app.Service
import android.support.test.InstrumentationRegistry
import android.content.Intent
import java.util.concurrent.TimeoutException
import org.junit.Test
import org.junit.Assert.assertThat
import android.support.test.rule.ServiceTestRule
import org.amshove.kluent.shouldBePositive
import org.junit.Rule

class DatabaseServicetest {
    @Rule
    val mServiceRule = ServiceTestRule()

    @Test
    @Throws(TimeoutException::class)
    fun testWithBoundService() {
        // Create the service Intent.
        val serviceIntent = Intent(InstrumentationRegistry.getTargetContext(),
                DatabaseService::class.java)

        // Data can be passed to the service via the Intent.
        serviceIntent.putExtra("DatabaseService.SEED_KEY", 42L)

        // Bind the service and grab a reference to the binder.
        val binder = mServiceRule.bindService(serviceIntent)

        // Get the reference to the service, or you can call
        // public methods on the binder directly.
        val service = (binder as DatabaseService.LocalBinder).service

        // Verify that the service is working correctly.
        service.randomNumber.shouldBePositive()
    }

}