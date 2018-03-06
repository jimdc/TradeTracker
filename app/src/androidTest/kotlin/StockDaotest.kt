import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.arch.lifecycle.LiveData
import com.example.group69.alarm.Stock
import com.example.group69.alarm.StockDao
import com.example.group69.alarm.StockDatabase
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
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
        var ssget = stockDao.findStockById(samplestock1.stockid) //Ensure that it's not just cached.
        assert(ssget == null)

        var subscriber: TestSubscriber<List<Stock>> = stockDao.getAllStocksF().test()

        stockDao.insert(samplestock1)
        ssget = stockDao.findStockById(samplestock1.stockid)
        assert(ssget == samplestock1)

        assert(!subscriber.hasSubscription()) //For now...
    }


    @Test
    fun getStocklistRetrievesData() {

        val stocklist = arrayOf(samplestock1, samplestock2, samplestock3)
        stocklist.forEach {
            stockDao.insert(it)
        }

        val retrievedstocks = stockDao.getAllStocks()
        assert(retrievedstocks == stocklist.sortedWith(compareBy({it.stockid}, {it.stockid})))
    }

    @Test
    fun clearStocklistClearsData() {
        stockDao.insert(samplestock1)
        stockDao.delete(samplestock1)
        assert(stockDao.getAllStocks().isEmpty())
    }
}