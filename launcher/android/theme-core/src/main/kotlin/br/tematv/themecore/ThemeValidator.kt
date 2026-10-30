package br.tematv.themecore

import java.io.File
import java.io.IOException

/** Valida uma pasta de tema já descompactada: manifesto, regras de campo e arquivos referenciados. */
class ThemeValidator {

    fun validate(themeDir: File): ThemeLoadResult = try {
        ThemeLoadResult.Loaded(validateOrThrow(themeDir))
    } catch (exception: ThemeRejectedException) {
        ThemeLoadResult.Rejected(exception.error)
    } catch (exception: IOException) {
        ThemeLoadResult.Rejected(ThemeError(ThemeErrorCode.THEME_IO_ERROR, detail = exception.message))
    }

    private fun validateOrThrow(themeDir: File): ValidatedTheme {
        val manifestFile = File(themeDir, MANIFEST_FILE_NAME)
        if (!manifestFile.isFile) reject(ThemeErrorCode.THEME_MANIFEST_MISSING)
        val manifest = ManifestParser.parse(manifestFile.readText(Charsets.UTF_8))

        val (logoFile, _) = requireImage(themeDir, manifest.logoImage, "logo_image", PNG_ONLY)
        val (frameFile, frameInfo) = requireImage(themeDir, manifest.iconFrame.image, "icon_frame.image", PNG_ONLY)
        val backgroundFile = manifest.backgroundImage?.let {
            requireImage(themeDir, it, "background_image", ImageFormat.entries.toSet()).first
        }
        val fontFile = manifest.fontFile?.let { requireFont(themeDir, it) }

        return ValidatedTheme(
            manifest = manifest,
            rootDir = themeDir,
            logoFile = logoFile,
            backgroundFile = backgroundFile,
            fontFile = fontFile,
            iconFrameFile = frameFile,
            iconFrameWidthPx = frameInfo.width,
            iconFrameHeightPx = frameInfo.height,
        )
    }

    private fun requireImage(
        themeDir: File,
        path: String,
        field: String,
        allowedFormats: Set<ImageFormat>,
    ): Pair<File, ImageInfo> {
        val file = File(themeDir, path)
        if (!file.isFile) reject(ThemeErrorCode.THEME_ASSET_MISSING, field, path)
        val info = ImageHeaderReader.read(file) ?: reject(ThemeErrorCode.THEME_ASSET_INVALID, field, "não é PNG, JPEG nem WEBP")
        if (info.format !in allowedFormats) {
            reject(ThemeErrorCode.THEME_ASSET_INVALID, field, "formato ${info.format} não aceito aqui")
        }
        return file to info
    }

    private fun requireFont(themeDir: File, path: String): File {
        val file = File(themeDir, path)
        if (!file.isFile) reject(ThemeErrorCode.THEME_ASSET_MISSING, "font_file", path)
        if (file.extension.lowercase() !in FONT_EXTENSIONS) {
            reject(ThemeErrorCode.THEME_ASSET_INVALID, "font_file", "use .ttf ou .otf")
        }
        val signature = file.inputStream().use { input -> ByteArray(4).also { input.read(it) } }
        if (FONT_SIGNATURES.none { it.contentEquals(signature) }) {
            reject(ThemeErrorCode.THEME_ASSET_INVALID, "font_file", "conteúdo não é TrueType nem OpenType")
        }
        return file
    }

    companion object {
        const val MANIFEST_FILE_NAME = "theme.json"
        private val PNG_ONLY = setOf(ImageFormat.PNG)
        private val FONT_EXTENSIONS = setOf("ttf", "otf")
        private val FONT_SIGNATURES = listOf(
            byteArrayOf(0x00, 0x01, 0x00, 0x00),
            "true".toByteArray(Charsets.US_ASCII),
            "OTTO".toByteArray(Charsets.US_ASCII),
        )
    }
}
