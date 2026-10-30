package br.tematv.launcher.home

import kotlin.random.Random

/** Sorteia o versículo cada vez que a tela inicial aparece, sem repetir o anterior (emenda E001 da feature 002). */
object VersePicker {

    fun next(verseCount: Int, previousIndex: Int?, random: Random = Random.Default): Int {
        require(verseCount > 0) { "o tema precisa de pelo menos um versículo" }
        if (verseCount == 1) return 0
        return (0 until verseCount).filter { it != previousIndex }.random(random)
    }
}
