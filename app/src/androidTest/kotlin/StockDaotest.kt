import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.advent.group69.tradetracker.model.Stock
import com.advent.group69.tradetracker.model.StockDao
import com.advent.group69.tradetracker.model.StockDatabase
import io.reactivex.subscribers.TestSubscriber
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
open class StockDaoTest {

    private lateinit var stocksDatabase: StockDatabase
    private lateinit var stockDao: StockDao

    val samplestock1 = Stock(12345, "JPM", 500.0, aboveB = true, phoneB = false, crypto = false)
    val samplestock2 = Stock(12346, "SNAP", 18.25, aboveB = false, phoneB = true, crypto = false)
    val samplestock3 = Stock(12347, "LTC", 211.12, aboveB = true, phoneB = true, crypto = true)

    @Before
    fun initDb() {
        val context = InstrumentationRegistry.getTargetContext()
        stocksDatabase = StockDatabase.getInstance(context) as StockDatabase
        stockDao = stocksDatabase.stockDao()
    }

    @After
    fun closeDb() {
        StockDatabase.destroyInstance()
    }

    @Test
    fun insertStockSavesData() {
        stockDao.delete(samplestock1)
        var ssget = stockDao.findStockById(samplestock1.stockid)
        assertNull("samplestock1 already in db before I added it?!", ssget)

        val subscriber: TestSubscriber<List<Stock>> = stockDao.getFlowableStocks().test()

        stockDao.insert(samplestock1)
        ssget = stockDao.findStockById(samplestock1.stockid)
        assertEquals("stockDao not finding sample stock I inserted!", ssget, samplestock1)

        assert(!subscriber.hasSubscription()) //For now...
    }


    @Test
    fun getStocklistRetrievesData() {
        stockDao.nukeall()

        val stocklist = arrayOf(samplestock1, samplestock2, samplestock3)
        stocklist.forEach { stockDao.insert(it) }

        val retrievedstocks = stockDao.getAllStocks()
        assertThat("Did not retrieve at least as many stocks as I put in",
                retrievedstocks, hasSize(greaterThanOrEqualTo(stocklist.size)))

        assertEquals("Did not return exact list of stocks I put in",
                retrievedstocks, stocklist.sortedWith(compareBy({it.stockid}, {it.stockid})))

        stocklist.forEach { stockDao.delete(it) }
        assertThat("stockDao did not delete stocks when I requested", stockDao.getAllStocks().isEmpty())
    }

    @Test
    fun clearStocklistClearsData() {
        stockDao.insert(samplestock1)
        assert(!stockDao.getAllStocks().isEmpty())
        stockDao.delete(samplestock1)
        assert(stockDao.getAllStocks().isEmpty())
    }
}