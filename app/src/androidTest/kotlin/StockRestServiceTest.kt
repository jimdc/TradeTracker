import android.content.Context
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.runner.AndroidJUnit4
import com.advent.tradetracker.model.StockRestService
import org.junit.runner.RunWith
import org.mockito.Mock
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import com.advent.tradetracker.viewmodel.MainActivity
import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import android.content.Intent
import android.support.test.InstrumentationRegistry
import com.advent.tradetracker.model.TradeTrackerConstants

@Rule
var mActivityRule = ActivityTestRule(MainActivity::class.java, true, false)

@RunWith(AndroidJUnit4::class)
class StockRestServiceTest {

    private val server = MockWebServer()

    @Before @Throws(IOException::class)
    fun setUp() {
        server.start()
        TradeTrackerConstants.CRYPTO_BASE_URL = server.url("/").toString()
    }

    @Test
    fun happyPath200() {
        val fileName = "200_crypto_found.json"
        server.enqueue(MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromFile(getInstrumentation().context, fileName)))

        val intent = Intent()
        mActivityRule.launchActivity(intent)

        /**
         * Check if the error dialog was launched
         */
    }

    @Test
    fun unhappyPath404() {
        val fileName = "404_crypto_not_found.json"
        server.enqueue(MockResponse()
                .setResponseCode(404)
                .setBody(getStringFromFile(getInstrumentation().context, fileName)))

        /**
         * Check if the error dialog was launched
         */
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Throws(Exception::class)
    private fun getStringFromFile(context: Context, filePath: String): String {
        val stream = context.resources.assets.open(filePath)
        val ret = convertStreamToString(stream)

        stream.close()
        return ret
    }

    @Throws(Exception::class)
    private fun convertStreamToString(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()

        var line = reader.readLine()
        while(line != null) {
            sb.append(line).append("\n")
            line = reader.readLine()
        }

        reader.close()
        return sb.toString()
    }
}

// following https://riggaroo.co.za/introduction-android-testing-part3/
/*
private var userRepository: UserRepository? = null

@Before
@Throws(Exception::class)
fun setUp() {
    MockitoAnnotations.initMocks(this)
    userRepository = UserRepositoryImpl(githubUserRestService)
}*/