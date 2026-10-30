package br.tematv.launcher.takeover

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HoldToConfirmTest {

    @Test
    fun progressGrowsWithHoldTimeAndConfirmsAtThreeSeconds() {
        val hold = HoldToConfirm()
        hold.press(nowMs = 1_000)

        assertEquals(0.5f, hold.progress(2_500), 0.001f)
        assertFalse(hold.isConfirmed(3_999))
        assertTrue(hold.isConfirmed(4_000))
        assertEquals(1f, hold.progress(9_000), 0.001f)
    }

    @Test
    fun releasingBeforeThreeSecondsCancels() {
        val hold = HoldToConfirm()
        hold.press(1_000)
        hold.release()

        assertFalse(hold.isHolding)
        assertEquals(0f, hold.progress(5_000), 0.001f)
        assertFalse(hold.isConfirmed(5_000))
    }

    @Test
    fun keyRepeatsDoNotRestartTheCount() {
        val hold = HoldToConfirm()
        hold.press(1_000)
        hold.press(2_000)
        hold.press(3_000)

        assertTrue(hold.isConfirmed(4_000))
    }

    @Test
    fun newPressAfterReleaseStartsFromZero() {
        val hold = HoldToConfirm()
        hold.press(1_000)
        hold.release()
        hold.press(10_000)

        assertEquals(0f, hold.progress(10_000), 0.001f)
        assertFalse(hold.isConfirmed(12_000))
        assertTrue(hold.isConfirmed(13_000))
    }
}
