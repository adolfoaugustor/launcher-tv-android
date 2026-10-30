package br.tematv.themecore

import br.tematv.themecore.TestPackages.withField
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ThemeValidatorTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val validator = ThemeValidator()

    private fun validate(
        manifest: String? = TestPackages.baseManifest().toString(),
        assets: Map<String, ByteArray> = TestPackages.defaultAssets(),
    ): ThemeLoadResult = validator.validate(TestPackages.writeThemeDir(temporaryFolder.newFolder(), manifest, assets))

    private fun validate(manifest: JsonObject, assets: Map<String, ByteArray> = TestPackages.defaultAssets()) =
        validate(manifest.toString(), assets)

    private fun ThemeLoadResult.loaded(): ValidatedTheme = when (this) {
        is ThemeLoadResult.Loaded -> theme
        is ThemeLoadResult.Rejected -> throw AssertionError("era esperado tema válido, veio $error")
    }

    private fun assertRejected(result: ThemeLoadResult, code: ThemeErrorCode, field: String? = null) {
        val rejected = result as? ThemeLoadResult.Rejected ?: throw AssertionError("era esperada a recusa $code $field")
        assertEquals(code, rejected.error.code)
        if (field != null) assertEquals(field, rejected.error.field)
    }

    @Test
    fun acceptsValidThemeAndExposesValues() {
        val theme = validate().loaded()

        assertEquals("igreja-teste", theme.manifest.themeId)
        assertEquals("#3B2A1A", theme.manifest.colors.text)
        assertEquals(FrameSlot(0.18, 0.20, 0.64, 0.42), theme.manifest.iconFrame.iconSlot)
        assertEquals(3, theme.manifest.verses.size)
        assertEquals(260, theme.iconFrameWidthPx)
        assertEquals(340, theme.iconFrameHeightPx)
        assertEquals(AspectRatio(13, 17), theme.iconFrameAspectRatio)
        assertTrue(theme.logoFile.isFile)
        assertTrue(theme.backgroundFile!!.isFile)
        assertNull(theme.fontFile)
    }

    @Test
    fun exposesReducedAspectRatioForLargerFrames() {
        val assets = TestPackages.defaultAssets() + (TestPackages.FRAME_FILE to TestPackages.pngBytes(520, 680))

        assertEquals(AspectRatio(13, 17), validate(assets = assets).loaded().iconFrameAspectRatio)
    }

    @Test
    fun rejectsMissingManifest() {
        assertRejected(validate(manifest = null), ThemeErrorCode.THEME_MANIFEST_MISSING)
    }

    @Test
    fun rejectsMalformedJson() {
        assertRejected(validate(manifest = "{ isto não é json"), ThemeErrorCode.THEME_MANIFEST_INVALID)
    }

    @Test
    fun rejectsJsonThatIsNotAnObject() {
        assertRejected(validate(manifest = "[1, 2, 3]"), ThemeErrorCode.THEME_MANIFEST_INVALID)
    }

    @Test
    fun rejectsUnsupportedSchemaVersion() {
        val manifest = TestPackages.baseManifest().withField("schema_version", 2)

        assertRejected(validate(manifest), ThemeErrorCode.THEME_SCHEMA_UNSUPPORTED, "schema_version")
    }

    @Test
    fun rejectsMissingSchemaVersion() {
        val manifest = TestPackages.baseManifest().withField("schema_version", null)

        assertRejected(validate(manifest), ThemeErrorCode.THEME_FIELD_INVALID, "schema_version")
    }

    @Test
    fun rejectsEachInvalidField() {
        val cases = listOf(
            Triple("theme_id", JsonPrimitive("Igreja X"), "theme_id"),
            Triple("theme_id", JsonPrimitive("ab"), "theme_id"),
            Triple("display_name", JsonPrimitive(""), "display_name"),
            Triple("colors.background", JsonPrimitive("FFF6E0"), "colors.background"),
            Triple("colors.text", JsonPrimitive("azul"), "colors.text"),
            Triple("colors.accent", null, "colors.accent"),
            Triple("logo_image", null, "logo_image"),
            Triple("message", JsonPrimitive("x".repeat(121)), "message"),
            Triple("message", JsonPrimitive(42), "message"),
            Triple("icon_frame.icon_slot.w", JsonPrimitive(0.9), "icon_frame.icon_slot"),
            Triple("icon_frame.label_slot.h", JsonPrimitive(0), "icon_frame.label_slot"),
            Triple("icon_frame.icon_slot.x", JsonPrimitive(-0.1), "icon_frame.icon_slot"),
            Triple("verses", JsonArray(emptyList()), "verses"),
            Triple("verses", TestPackages.versesJson(367), "verses"),
            Triple("verses_attribution", JsonPrimitive(""), "verses_attribution"),
        )
        cases.forEach { (path, value, expectedField) ->
            val result = validate(TestPackages.baseManifest().withField(path, value))
            assertRejected(result, ThemeErrorCode.THEME_FIELD_INVALID, expectedField)
        }
    }

    @Test
    fun rejectsInvalidVerseItemWithIndexInField() {
        val badVerse = buildJsonObject {
            put("reference", "Jo 3:16")
            put("text", "")
            put("version", "TST")
        }
        val verses = JsonArray(TestPackages.versesJson(2) + badVerse)

        assertRejected(validate(TestPackages.baseManifest().withField("verses", verses)), ThemeErrorCode.THEME_FIELD_INVALID, "verses[2].text")
    }

    @Test
    fun acceptsBoundaryValues() {
        val manifest = TestPackages.baseManifest()
            .withField("message", "x".repeat(120))
            .withField("verses", TestPackages.versesJson(366))

        assertEquals(366, validate(manifest).loaded().manifest.verses.size)
    }

    @Test
    fun countsMessageLengthInCharactersNotBytes() {
        val manifest = TestPackages.baseManifest().withField("message", "ç".repeat(120))

        validate(manifest).loaded()
    }

    @Test
    fun ignoresUnknownFields() {
        val manifest = TestPackages.baseManifest().withField("campo_futuro", "qualquer coisa")

        validate(manifest).loaded()
    }

    @Test
    fun rejectsMissingAsset() {
        val assets = TestPackages.defaultAssets() - TestPackages.LOGO_FILE

        assertRejected(validate(assets = assets), ThemeErrorCode.THEME_ASSET_MISSING, "logo_image")
    }

    @Test
    fun rejectsAssetWithWrongSignature() {
        val assets = TestPackages.defaultAssets() + (TestPackages.LOGO_FILE to "não é imagem".toByteArray())

        assertRejected(validate(assets = assets), ThemeErrorCode.THEME_ASSET_INVALID, "logo_image")
    }

    @Test
    fun rejectsLogoThatIsNotPng() {
        val assets = TestPackages.defaultAssets() + (TestPackages.LOGO_FILE to TestPackages.jpegBytes(10, 10))

        assertRejected(validate(assets = assets), ThemeErrorCode.THEME_ASSET_INVALID, "logo_image")
    }

    @Test
    fun acceptsJpegAndWebpBackgrounds() {
        val jpegAssets = TestPackages.defaultAssets() + (TestPackages.BACKGROUND_FILE to TestPackages.jpegBytes(1920, 1080))
        val webpAssets = TestPackages.defaultAssets() + (TestPackages.BACKGROUND_FILE to TestPackages.webpBytes(1920, 1080))

        validate(assets = jpegAssets).loaded()
        validate(assets = webpAssets).loaded()
    }

    @Test
    fun acceptsThemeWithoutBackgroundImage() {
        val manifest = TestPackages.baseManifest().withField("background_image", null)

        assertNull(validate(manifest).loaded().backgroundFile)
    }

    @Test
    fun rejectsAssetReferenceOutsideThePackage() {
        val manifest = TestPackages.baseManifest().withField("logo_image", "../logo.png")

        assertRejected(validate(manifest), ThemeErrorCode.THEME_FIELD_INVALID, "logo_image")
    }

    @Test
    fun acceptsOptionalFont() {
        val manifest = TestPackages.baseManifest().withField("font_file", "fonte.ttf")
        val assets = TestPackages.defaultAssets() + ("fonte.ttf" to TestPackages.ttfBytes())

        assertEquals("fonte.ttf", validate(manifest, assets).loaded().fontFile!!.name)
    }

    @Test
    fun rejectsFontWithWrongExtension() {
        val manifest = TestPackages.baseManifest().withField("font_file", "fonte.txt")
        val assets = TestPackages.defaultAssets() + ("fonte.txt" to TestPackages.ttfBytes())

        assertRejected(validate(manifest, assets), ThemeErrorCode.THEME_ASSET_INVALID, "font_file")
    }

    @Test
    fun rejectsFontWithWrongContent() {
        val manifest = TestPackages.baseManifest().withField("font_file", "fonte.ttf")
        val assets = TestPackages.defaultAssets() + ("fonte.ttf" to "não é fonte".toByteArray())

        assertRejected(validate(manifest, assets), ThemeErrorCode.THEME_ASSET_INVALID, "font_file")
    }

    @Test
    fun rejectsMissingDirectoryAsMissingManifest() {
        val missing = File(temporaryFolder.root, "nao-existe")

        assertRejected(validator.validate(missing), ThemeErrorCode.THEME_MANIFEST_MISSING)
    }
}
