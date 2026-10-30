package br.tematv.launcher.theme

import android.content.Context
import br.tematv.themecore.ThemeLoadResult
import br.tematv.themecore.ThemeValidator
import br.tematv.themecore.ValidatedTheme
import java.io.File

fun interface DefaultThemeSource {
    fun load(): ValidatedTheme
}

/**
 * Tema padrão embutido em `assets/default-theme/`. É copiado para `filesDir/default-theme-<versão>/`
 * uma vez por versão do app, porque o validador trabalha sobre arquivos.
 */
class DefaultThemeProvider(
    private val context: Context,
    private val validator: ThemeValidator = ThemeValidator(),
) : DefaultThemeSource {

    override fun load(): ValidatedTheme {
        val versionCode = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
        val targetDir = File(context.filesDir, "$DIRECTORY_PREFIX$versionCode")
        if (!File(targetDir, COMPLETE_MARKER).isFile) copyAssets(targetDir)
        deleteOtherVersions(targetDir)

        return when (val result = validator.validate(targetDir)) {
            is ThemeLoadResult.Loaded -> result.theme
            // Tema padrão inválido é erro de build, não de cliente: falha alto.
            is ThemeLoadResult.Rejected -> error("tema padrão embutido inválido: ${result.error}")
        }
    }

    private fun copyAssets(targetDir: File) {
        targetDir.deleteRecursively()
        targetDir.mkdirs()
        val assetNames = context.assets.list(ASSETS_DIRECTORY).orEmpty()
        check(assetNames.isNotEmpty()) { "assets/$ASSETS_DIRECTORY está vazio" }
        assetNames.forEach { name ->
            context.assets.open("$ASSETS_DIRECTORY/$name").use { input ->
                File(targetDir, name).outputStream().use { output -> input.copyTo(output) }
            }
        }
        File(targetDir, COMPLETE_MARKER).createNewFile()
    }

    private fun deleteOtherVersions(currentDir: File) {
        context.filesDir.listFiles()
            ?.filter { it.isDirectory && it.name.startsWith(DIRECTORY_PREFIX) && it != currentDir }
            ?.forEach { it.deleteRecursively() }
    }

    private companion object {
        const val ASSETS_DIRECTORY = "default-theme"
        const val DIRECTORY_PREFIX = "default-theme-"
        const val COMPLETE_MARKER = ".complete"
    }
}
