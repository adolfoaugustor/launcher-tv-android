package br.tematv.launcher.home

import java.time.LocalDate

/** Versículo do dia: o mesmo o dia todo, avançando um por dia e voltando ao primeiro depois do último (RN-03). */
object VerseOfTheDay {

    fun indexFor(date: LocalDate, verseCount: Int): Int {
        require(verseCount > 0) { "o tema precisa de pelo menos um versículo" }
        return Math.floorMod(date.toEpochDay(), verseCount.toLong()).toInt()
    }
}
