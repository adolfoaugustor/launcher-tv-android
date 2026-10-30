package br.tematv.launcher.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThemeImportNamesTest {

    @Test
    fun acceptsKebabCasePackagesAndTheActiveThemeFile() {
        assertEquals("biblia-exemplo.tematv", ThemeImportNames.fileNameFrom(listOf("biblia-exemplo.tematv")))
        assertEquals("igreja2.tematv", ThemeImportNames.fileNameFrom(listOf("igreja2.tematv")))
        assertEquals("active_theme.txt", ThemeImportNames.fileNameFrom(listOf("active_theme.txt")))
        assertEquals("device_code.txt", ThemeImportNames.fileNameFrom(listOf("device_code.txt")))
        assertEquals("activation_code.txt", ThemeImportNames.fileNameFrom(listOf("activation_code.txt")))
        assertEquals("app_status.json", ThemeImportNames.fileNameFrom(listOf("app_status.json")))
    }

    @Test
    fun appStatusIsReadOnly() {
        assertEquals(false, ThemeImportNames.isWritable("app_status.json"))
        assertEquals(true, ThemeImportNames.isWritable("activation_code.txt"))
        assertEquals(true, ThemeImportNames.isWritable("igreja.tematv"))
    }

    @Test
    fun rejectsNamesOutsideThePattern() {
        listOf(
            "Igreja X.tematv",
            "Igreja.tematv",
            "igreja.zip",
            "igreja.tematv.txt",
            ".tematv",
            "..",
            "../fora.tematv",
            "outro.txt",
        ).forEach { name -> assertNull(name, ThemeImportNames.fileNameFrom(listOf(name))) }
    }

    @Test
    fun rejectsUrisWithZeroOrSeveralSegments() {
        assertNull(ThemeImportNames.fileNameFrom(emptyList()))
        assertNull(ThemeImportNames.fileNameFrom(listOf("pasta", "igreja.tematv")))
    }
}
