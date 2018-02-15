import com.example.group69.alarm.Updaten
import com.example.group69.alarm.SQLiteSingleton
import com.example.group69.alarm.BuildConfig
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import android.os.Build.VERSION_CODES.LOLLIPOP

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(LOLLIPOP), packageName = "com.example.group69.alarm")

class SQLiteSingletontest {
    lateinit var dbHelper: SQLiteSingleton

    @Before
    fun setup() {
        dbHelper = SQLiteSingleton(RuntimeEnvironment.application)
        dbHelper.clearDbAndRecreate()
    }

    @Test
    fun testDbInsertion() {
        
    }

    @After
    fun tearDown() {
        dbHelper.clearDb()
    }
}