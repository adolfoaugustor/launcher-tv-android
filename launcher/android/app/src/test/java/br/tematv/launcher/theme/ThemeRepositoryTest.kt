package br.tematv.launcher.theme

import br.tematv.themecore.ThemeLoadResult
import br.tematv.themecore.ThemeValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ThemeRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    // Os testes unitários do Android rodam com a pasta do módulo :app como diretório de trabalho.
    private val defaultThemeDir = File("src/main/assets/default-theme")
    private val sampleThemesDir = File("../sample-themes")

    private val themesDir by lazy { temporaryFolder.newFolder("themes") }
    private val cacheRoot by lazy { temporaryFolder.newFolder("theme-cache") }
    private val themeState = InMemoryThemeState()
    private val logger = RecordingLogger()

    private fun repository(storage: ThemePackageStore = ThemeStorage(themesDir, cacheRoot)) = ThemeRepository(
        packageStore = storage,
        themeState = themeState,
        defaultThemeSource = { (ThemeValidator().validate(defaultThemeDir) as ThemeLoadResult.Loaded).theme },
        logger = logger,
        ioDispatcher = Dispatchers.Unconfined,
        clock = { Instant.parse("2026-10-29T12:00:00Z") },
    )

    private fun installSample(
        sampleName: String,
        packageName: String = "$sampleName.tematv",
        replacements: Map<String, String> = emptyMap(),
    ): String {
        ZipOutputStream(File(themesDir, packageName).outputStream()).use { zip ->
            File(sampleThemesDir, sampleName).listFiles()!!.sortedBy { it.name }.forEach { file ->
                zip.putNextEntry(ZipEntry(file.name))
                val replacement = replacements[file.name]
                zip.write(replacement?.toByteArray(Charsets.UTF_8) ?: file.readBytes())
                zip.closeEntry()
            }
        }
        return packageName
    }

    private fun activate(packageName: String) {
        File(themesDir, ThemeStorage.ACTIVE_THEME_FILE_NAME).writeText(packageName)
    }

    private fun brokenBibleManifest(): String =
        File(sampleThemesDir, "biblia-exemplo/theme.json").readText().replace("\"#3B2A1A\"", "\"azul\"")

    @Test
    fun usesDefaultThemeWhenNoPackageIsActive() = runTest {
        val repository = repository()

        val active = repository.reload()

        assertEquals(ThemeOrigin.Default, active.origin)
        assertEquals("padrao-biblico", active.theme.manifest.themeId)
        assertEquals("Deus te abençoe!", active.theme.manifest.message)
        assertEquals(active, repository.activeTheme.value)
        assertNull(themeState.lastError)
    }

    @Test
    fun appliesValidActivePackage() = runTest {
        activate(installSample("biblia-exemplo"))

        val active = repository().reload()

        assertEquals(ThemeOrigin.Package("biblia-exemplo.tematv"), active.origin)
        assertEquals("biblia-exemplo", active.theme.manifest.themeId)
        assertEquals("biblia-exemplo.tematv", themeState.lastValidPackage)
        assertTrue(logger.messages.contains("Tema aplicado: biblia-exemplo"))
    }

    @Test
    fun keepsLastValidThemeWhenNewPackageIsRejected() = runTest {
        val repository = repository()
        activate(installSample("biblia-exemplo"))
        repository.reload()

        activate(installSample("biblia-exemplo", "quebrado.tematv", mapOf("theme.json" to brokenBibleManifest())))

        val active = repository.reload()

        assertEquals("biblia-exemplo", active.theme.manifest.themeId)
        assertEquals("THEME_FIELD_INVALID", themeState.lastError?.code)
        assertEquals("colors.text", themeState.lastError?.field)
        assertEquals("quebrado.tematv", themeState.lastError?.packageName)
        assertEquals("2026-10-29T12:00:00Z", themeState.lastError?.recordedAt)
        assertTrue(logger.messages.any { it.startsWith("Pacote recusado: THEME_FIELD_INVALID colors.text") })
    }

    @Test
    fun usesDefaultThemeWhenFirstPackageIsRejected() = runTest {
        activate(installSample("biblia-exemplo", replacements = mapOf("theme.json" to brokenBibleManifest())))

        val active = repository().reload()

        assertEquals(ThemeOrigin.Default, active.origin)
        assertEquals("THEME_FIELD_INVALID", themeState.lastError?.code)
    }

    @Test
    fun usesDefaultThemeWhenActivePackageDoesNotExist() = runTest {
        activate("apagado.tematv")

        val active = repository().reload()

        assertEquals(ThemeOrigin.Default, active.origin)
        assertEquals("THEME_ACTIVE_NOT_FOUND", themeState.lastError?.code)
        assertTrue(logger.messages.any { it.startsWith("Pacote recusado: THEME_ACTIVE_NOT_FOUND") })
    }

    @Test
    fun usesDefaultThemeWhenStorageIsUnavailable() = runTest {
        val active = repository(ThemeStorage(themesDir = null, cacheRoot = cacheRoot)).reload()

        assertEquals(ThemeOrigin.Default, active.origin)
        assertEquals("THEME_IO_ERROR", themeState.lastError?.code)
    }

    @Test
    fun keepsOnlyTheActiveCacheAndAllPackagesWhenSwitchingThemes() = runTest {
        val repository = repository()
        activate(installSample("biblia-exemplo"))
        repository.reload()
        activate(installSample("hamburguer-exemplo"))

        val active = repository.reload()

        assertEquals("hamburguer-exemplo", active.theme.manifest.themeId)
        val cacheDirs = cacheRoot.listFiles()!!.map { it.name }
        assertEquals(1, cacheDirs.size)
        assertTrue(cacheDirs.single().startsWith("hamburguer-exemplo-"))
        assertTrue(File(themesDir, "biblia-exemplo.tematv").isFile)
    }

    @Test
    fun reusesCacheWhenTheSamePackageIsReloaded() = runTest {
        val repository = repository()
        activate(installSample("biblia-exemplo"))
        val first = repository.reload()

        val second = repository.reload()

        assertEquals(first.theme.rootDir, second.theme.rootDir)
        assertEquals(1, cacheRoot.listFiles()!!.size)
    }

    private class InMemoryThemeState : ThemeStatePersistence {
        override var lastValidPackage: String? = null
        override var lastValidCacheDir: File? = null
        override var lastError: RecordedThemeError? = null

        override fun saveLastValid(packageName: String, cacheDir: File) {
            lastValidPackage = packageName
            lastValidCacheDir = cacheDir
        }

        override fun saveError(error: RecordedThemeError) {
            lastError = error
        }
    }

    private class RecordingLogger : ThemeLogger {
        val messages = mutableListOf<String>()
        override fun info(message: String) {
            messages += message
        }

        override fun warn(message: String) {
            messages += message
        }
    }
}
