import com.example.group69.alarm.DatabaseSortaService
import android.support.test.InstrumentationRegistry
import android.content.Intent
import java.util.concurrent.TimeoutException
import org.junit.Test
import android.support.test.rule.ServiceTestRule
import org.junit.Rule

class DatabaseServicetest {
    @Rule
    val mServiceRule = ServiceTestRule()
/*
    @Test
    @Throws(TimeoutException::class)
    fun testWithBoundService() {
        // Create the service Intent.
        val serviceIntent = Intent(InstrumentationRegistry.getTargetContext(),
                DatabaseSortaService::class.java)

        // Data can be passed to the service via the Intent.
        serviceIntent.putExtra("DatabaseSortaService.SEED_KEY", 42L)

        // Bind the service and grab a reference to the binder.
        val binder = mServiceRule.bindService(serviceIntent)

        // Get the reference to the service, or you can call
        // public methods on the binder directly.
        val service = (binder as DatabaseSortaService.LocalBinder).service

        // Verify that the service is working correctly.
        service.randomNumber.shouldBePositive()
    }*/

}