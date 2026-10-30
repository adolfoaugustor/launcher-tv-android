package br.tematv.launcher.home

import br.tematv.themecore.FrameSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SlotGeometryTest {

    private val bibleIconSlot = FrameSlot(0.18, 0.20, 0.64, 0.42)
    private val tolerance = 0.01f

    private fun assertRect(expected: PixelRect, actual: PixelRect) {
        assertEquals(expected.left, actual.left, tolerance)
        assertEquals(expected.top, actual.top, tolerance)
        assertEquals(expected.right, actual.right, tolerance)
        assertEquals(expected.bottom, actual.bottom, tolerance)
    }

    @Test
    fun convertsSlotFractionsToPixels() {
        val rect = SlotGeometry.slotRect(bibleIconSlot, tileWidth = 260f, tileHeight = 340f)

        assertRect(PixelRect(46.8f, 68f, 213.2f, 210.8f), rect)
    }

    @Test
    fun fitsWideBannerToSlotWidthWithBandsAboveAndBelow() {
        val slot = SlotGeometry.slotRect(bibleIconSlot, 260f, 340f)

        val banner = SlotGeometry.fitInside(contentWidth = 320f, contentHeight = 180f, bounds = slot)

        assertRect(PixelRect(46.8f, 92.6f, 213.2f, 186.2f), banner)
    }

    @Test
    fun fitsSquareIconToSlotHeightCentered() {
        val slot = SlotGeometry.slotRect(bibleIconSlot, 260f, 340f)

        val icon = SlotGeometry.fitInside(contentWidth = 96f, contentHeight = 96f, bounds = slot)

        assertRect(PixelRect(58.6f, 68f, 201.4f, 210.8f), icon)
    }

    @Test
    fun fittedContentNeverLeavesTheSlot() {
        val slot = SlotGeometry.slotRect(bibleIconSlot, 260f, 340f)
        listOf(1f to 1000f, 1000f to 1f, 50f to 50f, 640f to 360f).forEach { (width, height) ->
            val fitted = SlotGeometry.fitInside(width, height, slot)
            assertTrue(fitted.left >= slot.left - tolerance && fitted.right <= slot.right + tolerance)
            assertTrue(fitted.top >= slot.top - tolerance && fitted.bottom <= slot.bottom + tolerance)
            // Tolerância relativa: proporções extremas como 1000:1 acumulam erro de ponto flutuante.
            assertEquals(1f, (fitted.width / fitted.height) / (width / height), 0.001f)
        }
    }
}
