package br.tematv.themecore

import java.io.EOFException
import java.io.File
import java.io.IOException
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * Descompacta um pacote recusando caminhos que saiam da pasta de destino (zip slip)
 * e pacotes que estourem os limites de entradas ou de tamanho descompactado (zip bomb).
 */
class SafeZipExtractor(
    private val maxEntries: Int = 200,
    private val maxTotalUncompressedBytes: Long = 40L * 1024 * 1024,
) {

    fun extract(zipFile: File, destination: File) {
        val zip = try {
            ZipFile(zipFile)
        } catch (exception: ZipException) {
            reject(ThemeErrorCode.THEME_ARCHIVE_CORRUPT, detail = exception.message)
        }
        zip.use {
            val entries = zip.entries().toList()
            if (entries.size > maxEntries) {
                reject(ThemeErrorCode.THEME_ARCHIVE_UNSAFE, detail = "${entries.size} entradas, máximo $maxEntries")
            }
            val destinationRoot = destination.canonicalFile
            if (!destinationRoot.isDirectory && !destinationRoot.mkdirs()) {
                throw IOException("não foi possível criar $destinationRoot")
            }

            var totalBytes = 0L
            val buffer = ByteArray(64 * 1024)
            for (entry in entries) {
                val target = safeTarget(destinationRoot, entry.name)
                if (entry.isDirectory) {
                    target.mkdirs()
                    continue
                }
                target.parentFile?.mkdirs()
                try {
                    zip.getInputStream(entry).use { input ->
                        target.outputStream().use { output ->
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                totalBytes += read
                                if (totalBytes > maxTotalUncompressedBytes) {
                                    reject(ThemeErrorCode.THEME_ARCHIVE_UNSAFE, detail = "mais de $maxTotalUncompressedBytes bytes descompactados")
                                }
                                output.write(buffer, 0, read)
                            }
                        }
                    }
                } catch (exception: ZipException) {
                    reject(ThemeErrorCode.THEME_ARCHIVE_CORRUPT, detail = exception.message)
                } catch (exception: EOFException) {
                    reject(ThemeErrorCode.THEME_ARCHIVE_CORRUPT, detail = exception.message)
                }
            }
        }
    }

    private fun safeTarget(destinationRoot: File, entryName: String): File {
        val looksUnsafe = entryName.startsWith("/") || entryName.contains('\\') ||
            DRIVE_LETTER.containsMatchIn(entryName) || entryName.split('/').any { it == ".." }
        if (looksUnsafe) reject(ThemeErrorCode.THEME_ARCHIVE_UNSAFE, detail = "entrada \"$entryName\"")

        val target = File(destinationRoot, entryName).canonicalFile
        if (!target.path.startsWith(destinationRoot.path + File.separator)) {
            reject(ThemeErrorCode.THEME_ARCHIVE_UNSAFE, detail = "entrada \"$entryName\"")
        }
        return target
    }

    private companion object {
        val DRIVE_LETTER = Regex("^[A-Za-z]:")
    }
}
