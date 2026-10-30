package br.tematv.launcher.theme

import android.content.Context
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/** Onde ficam os pacotes de tema e seus caches; separado do Android para os testes rodarem na JVM. */
interface ThemePackageStore {
    /** Nome do pacote em `active_theme.txt`, ou nulo se não houver tema ativo. */
    fun readActivePackageName(): String?

    /** Arquivo do pacote dentro da pasta de temas, ou nulo se o nome tentar sair dela. */
    fun packageFile(packageName: String): File?

    /** Pasta de cache do pacote, identificada pelo nome e pelo SHA-256 do conteúdo. */
    fun cacheDirFor(packageFile: File): File

    fun deleteCachesExcept(keep: Set<File>)
}

class ThemeStorage(
    private val themesDir: File?,
    private val cacheRoot: File,
) : ThemePackageStore {

    fun ensureThemesDir(): Boolean = themesDir != null && (themesDir.isDirectory || themesDir.mkdirs())

    override fun readActivePackageName(): String? {
        val activeFile = File(requireThemesDir(), ACTIVE_THEME_FILE_NAME)
        if (!activeFile.isFile) return null
        return activeFile.readText(Charsets.UTF_8).trim().ifEmpty { null }
    }

    override fun packageFile(packageName: String): File? {
        val escapesThemesDir = packageName.contains('/') || packageName.contains('\\') || packageName == ".."
        return if (escapesThemesDir) null else File(requireThemesDir(), packageName)
    }

    override fun cacheDirFor(packageFile: File): File =
        File(cacheRoot, "${packageFile.nameWithoutExtension}-${shortSha256(packageFile)}")

    override fun deleteCachesExcept(keep: Set<File>) {
        val keptPaths = keep.map { it.canonicalPath }.toSet()
        cacheRoot.listFiles()
            ?.filter { it.isDirectory && it.canonicalPath !in keptPaths }
            ?.forEach { it.deleteRecursively() }
    }

    private fun requireThemesDir(): File = themesDir ?: throw IOException("armazenamento externo do app indisponível")

    private fun shortSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }.take(SHORT_SHA_LENGTH)
    }

    companion object {
        const val ACTIVE_THEME_FILE_NAME = "active_theme.txt"
        private const val SHORT_SHA_LENGTH = 12

        fun fromContext(context: Context): ThemeStorage = ThemeStorage(
            themesDir = context.getExternalFilesDir(null)?.let { File(it, "themes") },
            cacheRoot = File(context.filesDir, "theme-cache"),
        )
    }
}
