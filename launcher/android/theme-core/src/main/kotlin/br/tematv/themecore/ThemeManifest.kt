package br.tematv.themecore

import java.io.File

enum class ThemeErrorCode {
    THEME_ACTIVE_NOT_FOUND,
    THEME_TOO_LARGE,
    THEME_ARCHIVE_CORRUPT,
    THEME_ARCHIVE_UNSAFE,
    THEME_MANIFEST_MISSING,
    THEME_MANIFEST_INVALID,
    THEME_SCHEMA_UNSUPPORTED,
    THEME_FIELD_INVALID,
    THEME_ASSET_MISSING,
    THEME_ASSET_INVALID,
    THEME_IO_ERROR,
}

data class ThemeError(
    val code: ThemeErrorCode,
    val field: String? = null,
    val detail: String? = null,
) {
    override fun toString(): String = listOfNotNull(code.name, field, detail?.let { "($it)" }).joinToString(" ")
}

class ThemeRejectedException(val error: ThemeError) : Exception(error.toString())

internal fun reject(code: ThemeErrorCode, field: String? = null, detail: String? = null): Nothing =
    throw ThemeRejectedException(ThemeError(code, field, detail))

sealed interface ThemeLoadResult {
    data class Loaded(val theme: ValidatedTheme) : ThemeLoadResult
    data class Rejected(val error: ThemeError) : ThemeLoadResult
}

data class ThemeColors(val background: String, val text: String, val accent: String)

/** Área retangular em frações (0 a 1) da imagem da moldura. */
data class FrameSlot(val x: Double, val y: Double, val w: Double, val h: Double)

data class IconFrame(val image: String, val iconSlot: FrameSlot, val labelSlot: FrameSlot)

data class Verse(val reference: String, val text: String, val version: String)

/** Conteúdo do `theme.json`, esquema versão 1. */
data class ThemeManifest(
    val schemaVersion: Int,
    val themeId: String,
    val displayName: String,
    val colors: ThemeColors,
    val backgroundImage: String?,
    val logoImage: String,
    val message: String,
    val fontFile: String?,
    val iconFrame: IconFrame,
    val verses: List<Verse>,
    val versesAttribution: String,
)

data class AspectRatio(val width: Int, val height: Int) {
    companion object {
        fun reduced(width: Int, height: Int): AspectRatio {
            val divisor = greatestCommonDivisor(width, height)
            return AspectRatio(width / divisor, height / divisor)
        }

        private tailrec fun greatestCommonDivisor(a: Int, b: Int): Int = if (b == 0) a else greatestCommonDivisor(b, a % b)
    }
}

/** Tema que passou por todas as validações, com os arquivos já resolvidos dentro de [rootDir]. */
data class ValidatedTheme(
    val manifest: ThemeManifest,
    val rootDir: File,
    val logoFile: File,
    val backgroundFile: File?,
    val fontFile: File?,
    val iconFrameFile: File,
    val iconFrameWidthPx: Int,
    val iconFrameHeightPx: Int,
) {
    val iconFrameAspectRatio: AspectRatio get() = AspectRatio.reduced(iconFrameWidthPx, iconFrameHeightPx)
}
