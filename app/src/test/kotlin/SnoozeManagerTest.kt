import com.advent.tradetracker.model.SnoozeManager
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.junit.Test

class SnoozeManagerTest {

    @Test
    fun snoozesFor100Milliseconds() {
        com.advent.tradetracker.model.SnoozeManager.startSnooze(100, forceRestart = true) shouldBe true
        while(com.advent.tradetracker.model.SnoozeManager.isSnoozing()) {
            com.advent.tradetracker.model.SnoozeManager.getSnoozeTimeRemaining() shouldBeLessOrEqualTo 100
        }
    }

    @Test
    fun canStopSnoozeSuccessfully() {
        com.advent.tradetracker.model.SnoozeManager.startSnooze(10000, forceRestart = true) shouldBe true
        com.advent.tradetracker.model.SnoozeManager.stopSnooze()
        com.advent.tradetracker.model.SnoozeManager.isSnoozing() shouldBe false
    }

    @Test
    fun canForceRestartSnoozeSuccessfully() {
        com.advent.tradetracker.model.SnoozeManager.startSnooze(99999, forceRestart = true) shouldBe true
        com.advent.tradetracker.model.SnoozeManager.startSnooze(99, forceRestart = true) shouldBe true
        com.advent.tradetracker.model.SnoozeManager.getSnoozeTimeRemaining() shouldBeLessOrEqualTo 99
    }

}