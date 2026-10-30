package br.tematv.themecore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

/** Garante que o tema padrão embutido no app e os pacotes de exemplo passam na validação. */
class BundledThemesTest {

    private val projectRoot = File(
        System.getProperty("tematv.projectRoot") ?: error("propriedade tematv.projectRoot não definida pelo Gradle"),
    )

    private fun validate(relativePath: String): ValidatedTheme =
        when (val result = ThemeValidator().validate(File(projectRoot, relativePath))) {
            is ThemeLoadResult.Loaded -> result.theme
            is ThemeLoadResult.Rejected -> throw AssertionError("$relativePath inválido: ${result.error}")
        }

    @Test
    fun defaultThemeIsTheBibleThemeWithoutChurch() {
        val theme = validate("app/src/main/assets/default-theme")

        assertEquals("Deus te abençoe!", theme.manifest.message)
        assertEquals("frame-biblia.png", theme.iconFrameFile.name)
        assertEquals(AspectRatio(13, 17), theme.iconFrameAspectRatio)
    }

    @Test
    fun sampleThemesUseDifferentFramesWithTheSameFormat() {
        val bible = validate("sample-themes/biblia-exemplo")
        val burger = validate("sample-themes/hamburguer-exemplo")

        assertFalse(bible.iconFrameFile.readBytes().contentEquals(burger.iconFrameFile.readBytes()))
        assertEquals(bible.iconFrameAspectRatio, burger.iconFrameAspectRatio)
        assertEquals(260, burger.iconFrameWidthPx)
    }
}
