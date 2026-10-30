package br.tematv.launcher.theme

import br.tematv.themecore.ThemeError
import br.tematv.themecore.ThemeErrorCode
import br.tematv.themecore.ThemeLoadResult
import br.tematv.themecore.ThemePackageLoader
import br.tematv.themecore.ThemeValidator
import br.tematv.themecore.ValidatedTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.Instant

sealed interface ThemeOrigin {
    data class Package(val packageName: String) : ThemeOrigin
    data object Default : ThemeOrigin
}

data class ActiveTheme(val theme: ValidatedTheme, val origin: ThemeOrigin)

interface ThemeLogger {
    fun info(message: String)
    fun warn(message: String)
}

/**
 * Decide qual tema está ativo e avisa os inscritos em [activeTheme].
 * Regras: pacote válido é aplicado; pacote recusado mantém o último válido (ou o padrão);
 * sem pacote ativo, pacote inexistente ou falha de leitura, vale o tema padrão.
 */
class ThemeRepository(
    private val packageStore: ThemePackageStore,
    private val themeState: ThemeStatePersistence,
    private val defaultThemeSource: DefaultThemeSource,
    private val logger: ThemeLogger,
    private val ioDispatcher: CoroutineDispatcher,
    private val packageLoader: ThemePackageLoader = ThemePackageLoader(),
    private val validator: ThemeValidator = ThemeValidator(),
    private val clock: () -> Instant = Instant::now,
) {

    private val reloadMutex = Mutex()
    private val mutableActiveTheme = MutableStateFlow<ActiveTheme?>(null)

    /** Nulo até a primeira leitura terminar. */
    val activeTheme: StateFlow<ActiveTheme?> = mutableActiveTheme.asStateFlow()

    val lastError: RecordedThemeError? get() = themeState.lastError

    suspend fun reload(): ActiveTheme = reloadMutex.withLock {
        val theme = withContext(ioDispatcher) { resolveActiveTheme() }
        publish(theme)
        theme
    }

    private fun resolveActiveTheme(): ActiveTheme {
        val packageName = try {
            packageStore.readActivePackageName()
        } catch (exception: IOException) {
            recordRejection(ThemeError(ThemeErrorCode.THEME_IO_ERROR, detail = exception.message), packageName = null)
            return defaultTheme()
        }
        if (packageName == null) return defaultTheme()

        val packageFile = packageStore.packageFile(packageName)
        if (packageFile == null || !packageFile.isFile) {
            recordRejection(ThemeError(ThemeErrorCode.THEME_ACTIVE_NOT_FOUND), packageName)
            return defaultTheme()
        }

        return when (val result = loadPackage(packageFile)) {
            is ThemeLoadResult.Loaded -> {
                themeState.saveLastValid(packageName, result.theme.rootDir)
                packageStore.deleteCachesExcept(setOf(result.theme.rootDir))
                ActiveTheme(result.theme, ThemeOrigin.Package(packageName))
            }
            is ThemeLoadResult.Rejected -> {
                recordRejection(result.error, packageName)
                lastValidTheme() ?: defaultTheme()
            }
        }
    }

    private fun loadPackage(packageFile: File): ThemeLoadResult = try {
        val cacheDir = packageStore.cacheDirFor(packageFile)
        val cached = if (cacheDir.isDirectory) validator.validate(cacheDir) else null
        cached as? ThemeLoadResult.Loaded ?: packageLoader.load(packageFile, cacheDir)
    } catch (exception: IOException) {
        ThemeLoadResult.Rejected(ThemeError(ThemeErrorCode.THEME_IO_ERROR, detail = exception.message))
    }

    private fun lastValidTheme(): ActiveTheme? {
        val packageName = themeState.lastValidPackage ?: return null
        val cacheDir = themeState.lastValidCacheDir ?: return null
        val result = validator.validate(cacheDir) as? ThemeLoadResult.Loaded ?: return null
        return ActiveTheme(result.theme, ThemeOrigin.Package(packageName))
    }

    private fun defaultTheme() = ActiveTheme(defaultThemeSource.load(), ThemeOrigin.Default)

    private fun publish(theme: ActiveTheme) {
        mutableActiveTheme.value = theme
        val suffix = if (theme.origin == ThemeOrigin.Default) " (padrão)" else ""
        logger.info("Tema aplicado: ${theme.theme.manifest.themeId}$suffix")
    }

    private fun recordRejection(error: ThemeError, packageName: String?) {
        themeState.saveError(
            RecordedThemeError(
                code = error.code.name,
                field = error.field,
                packageName = packageName,
                recordedAt = clock().toString(),
            ),
        )
        val details = listOfNotNull(error.field, packageName?.let { "($it)" }, error.detail?.let { "- $it" })
        logger.warn((listOf("Pacote recusado: ${error.code.name}") + details).joinToString(" "))
    }
}
