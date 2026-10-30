package br.tematv.themecore

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

/** Lê o `theme.json` e aplica as regras de campo do esquema v1 (data-delta.md §1.1). */
internal object ManifestParser {

    const val SUPPORTED_SCHEMA_VERSION = 1
    const val MAX_MESSAGE_LENGTH = 120
    const val MAX_VERSES = 366

    private val themeIdPattern = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")
    private val colorPattern = Regex("^#[0-9A-Fa-f]{6}$")
    private const val SLOT_TOLERANCE = 1e-9

    fun parse(jsonText: String): ThemeManifest {
        val root = try {
            Json.parseToJsonElement(jsonText)
        } catch (exception: SerializationException) {
            reject(ThemeErrorCode.THEME_MANIFEST_INVALID, detail = exception.message)
        }
        val manifest = root as? JsonObject ?: reject(ThemeErrorCode.THEME_MANIFEST_INVALID, detail = "a raiz não é um objeto")

        val schemaVersion = (manifest["schema_version"] as? JsonPrimitive)?.takeUnless { it.isString }?.intOrNull
            ?: reject(ThemeErrorCode.THEME_FIELD_INVALID, "schema_version")
        if (schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            reject(ThemeErrorCode.THEME_SCHEMA_UNSUPPORTED, "schema_version", "versão $schemaVersion")
        }

        val themeId = manifest.requiredText("theme_id", "theme_id", 3..40)
        if (!themeIdPattern.matches(themeId)) reject(ThemeErrorCode.THEME_FIELD_INVALID, "theme_id")

        val colors = manifest.requiredObject("colors", "colors")
        val iconFrame = manifest.requiredObject("icon_frame", "icon_frame")

        return ThemeManifest(
            schemaVersion = schemaVersion,
            themeId = themeId,
            displayName = manifest.requiredText("display_name", "display_name", 1..60),
            colors = ThemeColors(
                background = colors.requiredColor("background", "colors.background"),
                text = colors.requiredColor("text", "colors.text"),
                accent = colors.requiredColor("accent", "colors.accent"),
            ),
            backgroundImage = manifest.optionalAssetPath("background_image", "background_image"),
            logoImage = manifest.requiredAssetPath("logo_image", "logo_image"),
            message = manifest.requiredText("message", "message", 1..MAX_MESSAGE_LENGTH),
            fontFile = manifest.optionalAssetPath("font_file", "font_file"),
            iconFrame = IconFrame(
                image = iconFrame.requiredAssetPath("image", "icon_frame.image"),
                iconSlot = iconFrame.requiredSlot("icon_slot", "icon_frame.icon_slot"),
                labelSlot = iconFrame.requiredSlot("label_slot", "icon_frame.label_slot"),
            ),
            verses = manifest.requiredVerses(),
            versesAttribution = manifest.requiredText("verses_attribution", "verses_attribution", 1..120),
        )
    }

    private fun JsonObject.requiredText(key: String, field: String, lengthRange: IntRange): String {
        val value = (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
            ?: reject(ThemeErrorCode.THEME_FIELD_INVALID, field)
        if (value.codePointCount(0, value.length) !in lengthRange) reject(ThemeErrorCode.THEME_FIELD_INVALID, field)
        return value
    }

    private fun JsonObject.requiredObject(key: String, field: String): JsonObject =
        this[key] as? JsonObject ?: reject(ThemeErrorCode.THEME_FIELD_INVALID, field)

    private fun JsonObject.requiredColor(key: String, field: String): String {
        val value = requiredText(key, field, 7..7)
        if (!colorPattern.matches(value)) reject(ThemeErrorCode.THEME_FIELD_INVALID, field)
        return value
    }

    private fun JsonObject.requiredAssetPath(key: String, field: String): String {
        val path = requiredText(key, field, 1..120)
        val segments = path.split('/')
        val escapesPackage = path.startsWith("/") || path.contains('\\') || path.contains(':') || segments.any { it == ".." }
        if (escapesPackage) reject(ThemeErrorCode.THEME_FIELD_INVALID, field)
        return path
    }

    private fun JsonObject.optionalAssetPath(key: String, field: String): String? =
        if (this[key] == null || this[key] is JsonNull) null else requiredAssetPath(key, field)

    private fun JsonObject.requiredSlot(key: String, field: String): FrameSlot {
        val slot = requiredObject(key, field)
        fun coordinate(name: String): Double =
            (slot[name] as? JsonPrimitive)?.takeUnless { it.isString }?.doubleOrNull?.takeIf { it in 0.0..1.0 }
                ?: reject(ThemeErrorCode.THEME_FIELD_INVALID, field)

        val frameSlot = FrameSlot(coordinate("x"), coordinate("y"), coordinate("w"), coordinate("h"))
        val fitsInsideFrame = frameSlot.w > 0 && frameSlot.h > 0 &&
            frameSlot.x + frameSlot.w <= 1 + SLOT_TOLERANCE && frameSlot.y + frameSlot.h <= 1 + SLOT_TOLERANCE
        if (!fitsInsideFrame) reject(ThemeErrorCode.THEME_FIELD_INVALID, field)
        return frameSlot
    }

    private fun JsonObject.requiredVerses(): List<Verse> {
        val verses = this["verses"] as? JsonArray ?: reject(ThemeErrorCode.THEME_FIELD_INVALID, "verses")
        if (verses.size !in 1..MAX_VERSES) reject(ThemeErrorCode.THEME_FIELD_INVALID, "verses")
        return verses.mapIndexed { index, element ->
            val field = "verses[$index]"
            val verse = element as? JsonObject ?: reject(ThemeErrorCode.THEME_FIELD_INVALID, field)
            Verse(
                reference = verse.requiredText("reference", "$field.reference", 1..40),
                text = verse.requiredText("text", "$field.text", 1..600),
                version = verse.requiredText("version", "$field.version", 2..10),
            )
        }
    }
}
