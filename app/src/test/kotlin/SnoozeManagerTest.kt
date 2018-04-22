import com.advent.tradetracker.model.SnoozeManager
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.junit.Test

class SnoozeManagerTest {

    @Test
    fun snoozesFor100Milliseconds() {
        SnoozeManager.startSnooze(100, forceRestart = true) shouldBe true
        while(SnoozeManager.isSnoozing()) {
            SnoozeManager.getSnoozeTimeRemaining() shouldBeLessOrEqualTo 100
        }
    }

    @Test
    fun canStopSnoozeSuccessfully() {
        SnoozeManager.startSnooze(10000, forceRestart = true) shouldBe true
        SnoozeManager.stopSnooze()
        SnoozeManager.isSnoozing() shouldBe false
    }

    @Test
    fun canForceRestartSnoozeSuccessfully() {
        SnoozeManager.startSnooze(99999, forceRestart = true) shouldBe true
        SnoozeManager.startSnooze(99, forceRestart = true) shouldBe true
        SnoozeManager.getSnoozeTimeRemaining() shouldBeLessOrEqualTo 99
    }

}