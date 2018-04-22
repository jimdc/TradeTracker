
import com.advent.tradetracker.model.StockRestService
import org.mockito.Mock

@Mock
val stockRestService = StockRestService

// following https://riggaroo.co.za/introduction-android-testing-part3/
/*
private var userRepository: UserRepository? = null

@Before
@Throws(Exception::class)
fun setUp() {
    MockitoAnnotations.initMocks(this)
    userRepository = UserRepositoryImpl(githubUserRestService)
}*/