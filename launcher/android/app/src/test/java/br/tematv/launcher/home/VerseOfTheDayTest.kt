package br.tematv.launcher.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class VerseOfTheDayTest {

    @Test
    fun sameDayGivesSameIndex() {
        val day = LocalDate.of(2026, 10, 30)

        assertEquals(VerseOfTheDay.indexFor(day, 7), VerseOfTheDay.indexFor(day, 7))
    }

    @Test
    fun advancesOneVersePerDayAndWrapsAround() {
        val start = LocalDate.of(2026, 10, 30)
        val firstIndex = VerseOfTheDay.indexFor(start, 7)

        val indexes = (0L until 8L).map { VerseOfTheDay.indexFor(start.plusDays(it), 7) }

        assertEquals((0 until 8).map { (firstIndex + it) % 7 }, indexes)
    }

    @Test
    fun usesDaysSinceEpoch() {
        assertEquals(0, VerseOfTheDay.indexFor(LocalDate.of(1970, 1, 1), 7))
        assertEquals(3, VerseOfTheDay.indexFor(LocalDate.of(1970, 1, 4), 7))
    }

    @Test
    fun handlesDatesBeforeEpochWithoutNegativeIndex() {
        assertEquals(6, VerseOfTheDay.indexFor(LocalDate.of(1969, 12, 31), 7))
    }

    @Test
    fun singleVerseIsAlwaysChosen() {
        assertEquals(0, VerseOfTheDay.indexFor(LocalDate.of(2026, 10, 30), 1))
    }
}
