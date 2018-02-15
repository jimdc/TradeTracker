import org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import com.example.group69.alarm.Utility
import org.amshove.kluent.shouldEqual

class Utilitytest {

    @Test
    fun shouldFormatDollarsCorrectly() {
        Utility.toDollar("2.0") shouldEqual "$2.00"
    }

}