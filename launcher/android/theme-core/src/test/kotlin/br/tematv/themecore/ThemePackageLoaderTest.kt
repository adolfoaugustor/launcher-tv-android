package br.tematv.themecore

import br.tematv.themecore.TestPackages.withField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.RandomAccessFile

class ThemePackageLoaderTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val loader = ThemePackageLoader()

    private fun packageFile(entries: List<Pair<String, ByteArray>> = TestPackages.themeEntries()): File =
        TestPackages.writeTemaTv(File(temporaryFolder.root, "pacotes/igreja-teste.tematv"), entries)

    private fun extractionDir() = File(temporaryFolder.root, "cache/igreja-teste-abc")

    private fun assertRejected(result: ThemeLoadResult, code: ThemeErrorCode) {
        val rejected = result as? ThemeLoadResult.Rejected ?: throw AssertionError("era esperada a recusa $code")
        assertEquals(code, rejected.error.code)
    }

    @Test
    fun loadsValidPackageIntoExtractionDir() {
        val result = loader.load(packageFile(), extractionDir())

        val theme = (result as ThemeLoadResult.Loaded).theme
        assertEquals("igreja-teste", theme.manifest.themeId)
        assertEquals(extractionDir().canonicalFile, theme.rootDir.canonicalFile)
        assertTrue(File(extractionDir(), "theme.json").isFile)
        assertNull(theme.fontFile)
    }

    @Test
    fun replacesPreviousContentOfExtractionDir() {
        val leftover = File(extractionDir(), "sobra.txt").apply { parentFile.mkdirs(); writeText("antigo") }

        loader.load(packageFile(), extractionDir())

        assertFalse(leftover.exists())
    }

    @Test
    fun rejectsPackageWithoutManifestAndCleansUp() {
        val result = loader.load(packageFile(TestPackages.themeEntries(manifest = null)), extractionDir())

        assertRejected(result, ThemeErrorCode.THEME_MANIFEST_MISSING)
        assertFalse(extractionDir().exists())
    }

    @Test
    fun rejectsMalformedManifest() {
        val result = loader.load(packageFile(TestPackages.themeEntries(manifest = "{ quebrado")), extractionDir())

        assertRejected(result, ThemeErrorCode.THEME_MANIFEST_INVALID)
    }

    @Test
    fun rejectsPackageAboveSizeLimit() {
        val bigPackage = File(temporaryFolder.root, "grande.tematv")
        RandomAccessFile(bigPackage, "rw").use { it.setLength(16L * 1024 * 1024) }

        assertRejected(loader.load(bigPackage, extractionDir()), ThemeErrorCode.THEME_TOO_LARGE)
    }

    @Test
    fun loadsOptionalFontWhenPresent() {
        val manifest = TestPackages.baseManifest().withField("font_file", "fonte.ttf").toString()
        val assets = TestPackages.defaultAssets() + ("fonte.ttf" to TestPackages.ttfBytes())

        val result = loader.load(packageFile(TestPackages.themeEntries(manifest, assets)), extractionDir())

        assertEquals("fonte.ttf", (result as ThemeLoadResult.Loaded).theme.fontFile!!.name)
    }

    @Test
    fun reportsIoErrorForMissingPackageFile() {
        val result = loader.load(File(temporaryFolder.root, "nao-existe.tematv"), extractionDir())

        assertRejected(result, ThemeErrorCode.THEME_IO_ERROR)
    }
}
