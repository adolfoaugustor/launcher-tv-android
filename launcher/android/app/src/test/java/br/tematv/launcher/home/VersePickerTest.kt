package br.tematv.launcher.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class VersePickerTest {

    @Test
    fun neverRepeatsThePreviousVerse() {
        val random = Random(42)
        var previous: Int? = null
        repeat(500) {
            val next = VersePicker.next(20, previous, random)
            assertNotEquals(previous, next)
            assertTrue(next in 0 until 20)
            previous = next
        }
    }

    @Test
    fun reachesEveryVerse() {
        val random = Random(7)
        val seen = (1..500).map { VersePicker.next(20, null, random) }.toSet()

        assertEquals((0 until 20).toSet(), seen)
    }

    @Test
    fun singleVerseIsAlwaysChosen() {
        assertEquals(0, VersePicker.next(1, 0))
        assertEquals(0, VersePicker.next(1, null))
    }

    @Test
    fun previousIndexOutsideTheListIsIgnored() {
        assertTrue(VersePicker.next(3, 9, Random(1)) in 0 until 3)
    }
}
