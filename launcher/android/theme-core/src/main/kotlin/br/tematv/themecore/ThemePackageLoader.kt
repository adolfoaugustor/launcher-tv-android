package br.tematv.themecore

import java.io.File
import java.io.IOException

/** Abre um `.tematv`: confere o tamanho, descompacta com segurança na pasta de extração e valida. */
class ThemePackageLoader(
    private val extractor: SafeZipExtractor = SafeZipExtractor(),
    private val validator: ThemeValidator = ThemeValidator(),
    private val maxPackageBytes: Long = 15L * 1024 * 1024,
) {

    fun load(packageFile: File, extractionDir: File): ThemeLoadResult {
        val result = try {
            if (packageFile.length() > maxPackageBytes) {
                reject(ThemeErrorCode.THEME_TOO_LARGE, detail = "${packageFile.length()} bytes, máximo $maxPackageBytes")
            }
            if (extractionDir.exists() && !extractionDir.deleteRecursively()) {
                throw IOException("não foi possível limpar $extractionDir")
            }
            extractor.extract(packageFile, extractionDir)
            validator.validate(extractionDir)
        } catch (exception: ThemeRejectedException) {
            ThemeLoadResult.Rejected(exception.error)
        } catch (exception: IOException) {
            ThemeLoadResult.Rejected(ThemeError(ThemeErrorCode.THEME_IO_ERROR, detail = exception.message))
        }
        if (result is ThemeLoadResult.Rejected) extractionDir.deleteRecursively()
        return result
    }
}
