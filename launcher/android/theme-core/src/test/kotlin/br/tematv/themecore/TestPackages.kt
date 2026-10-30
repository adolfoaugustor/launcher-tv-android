package br.tematv.themecore

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Monta temas e pacotes `.tematv` para os testes. */
object TestPackages {

    const val LOGO_FILE = "logo.png"
    const val FRAME_FILE = "frame.png"
    const val BACKGROUND_FILE = "fundo.png"

    fun baseManifest(): JsonObject = buildJsonObject {
        put("schema_version", 1)
        put("theme_id", "igreja-teste")
        put("display_name", "Igreja Teste")
        putJsonObject("colors") {
            put("background", "#FFF6E0")
            put("text", "#3B2A1A")
            put("accent", "#E8A317")
        }
        put("background_image", BACKGROUND_FILE)
        put("logo_image", LOGO_FILE)
        put("message", "Bem-vindo!")
        putJsonObject("icon_frame") {
            put("image", FRAME_FILE)
            putJsonObject("icon_slot") { put("x", 0.18); put("y", 0.20); put("w", 0.64); put("h", 0.42) }
            putJsonObject("label_slot") { put("x", 0.12); put("y", 0.70); put("w", 0.76); put("h", 0.14) }
        }
        put("verses", versesJson(3))
        put("verses_attribution", "Texto de teste")
    }

    fun versesJson(count: Int): JsonArray = buildJsonArray {
        repeat(count) { index ->
            add(buildJsonObject {
                put("reference", "Ref ${index + 1}")
                put("text", "Texto do versículo ${index + 1}")
                put("version", "TST")
            })
        }
    }

    /** Devolve o manifesto com o campo do caminho pontilhado trocado por [value], ou removido se [value] for nulo. */
    fun JsonObject.withField(dottedPath: String, value: JsonElement?): JsonObject {
        val key = dottedPath.substringBefore('.')
        val rest = dottedPath.substringAfter('.', missingDelimiterValue = "")
        val updated = toMutableMap()
        if (rest.isEmpty()) {
            if (value == null) updated.remove(key) else updated[key] = value
        } else {
            val child = this[key] as? JsonObject ?: JsonObject(emptyMap())
            updated[key] = child.withField(rest, value)
        }
        return JsonObject(updated)
    }

    fun JsonObject.withField(dottedPath: String, value: String): JsonObject = withField(dottedPath, JsonPrimitive(value))

    fun JsonObject.withField(dottedPath: String, value: Number): JsonObject = withField(dottedPath, JsonPrimitive(value))

    fun defaultAssets(): Map<String, ByteArray> = mapOf(
        LOGO_FILE to pngBytes(10, 10),
        FRAME_FILE to pngBytes(260, 340),
        BACKGROUND_FILE to pngBytes(32, 18),
    )

    fun writeThemeDir(
        directory: File,
        manifest: String? = baseManifest().toString(),
        assets: Map<String, ByteArray> = defaultAssets(),
    ): File {
        directory.mkdirs()
        manifest?.let { File(directory, "theme.json").writeText(it, Charsets.UTF_8) }
        assets.forEach { (name, bytes) -> File(directory, name).apply { parentFile.mkdirs() }.writeBytes(bytes) }
        return directory
    }

    fun themeEntries(
        manifest: String? = baseManifest().toString(),
        assets: Map<String, ByteArray> = defaultAssets(),
    ): List<Pair<String, ByteArray>> =
        listOfNotNull(manifest?.let { "theme.json" to it.toByteArray(Charsets.UTF_8) }) + assets.toList()

    fun zipBytes(entries: List<Pair<String, ByteArray>>): ByteArray {
        val buffer = ByteArrayOutputStream()
        ZipOutputStream(buffer).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return buffer.toByteArray()
    }

    fun writeTemaTv(file: File, entries: List<Pair<String, ByteArray>> = themeEntries()): File {
        file.parentFile.mkdirs()
        file.writeBytes(zipBytes(entries))
        return file
    }

    fun pngBytes(width: Int, height: Int): ByteArray {
        fun chunk(type: String, data: ByteArray): ByteArray {
            val typeBytes = type.toByteArray(Charsets.US_ASCII)
            val crc = CRC32().apply { update(typeBytes); update(data) }
            return intBytes(data.size) + typeBytes + data + intBytes(crc.value.toInt())
        }

        val header = intBytes(width) + intBytes(height) + byteArrayOf(8, 6, 0, 0, 0)
        val rawRows = ByteArray(height * (1 + width * 4))
        val deflater = Deflater().apply { setInput(rawRows); finish() }
        val compressed = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (!deflater.finished()) compressed.write(buffer, 0, deflater.deflate(buffer))
        deflater.end()
        return byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) +
            chunk("IHDR", header) + chunk("IDAT", compressed.toByteArray()) + chunk("IEND", ByteArray(0))
    }

    fun jpegBytes(width: Int, height: Int): ByteArray {
        val startOfFrame = byteArrayOf(0xFF.toByte(), 0xC0.toByte(), 0x00, 0x11, 0x08) +
            shortBytes(height) + shortBytes(width) + byteArrayOf(0x03) + ByteArray(9)
        val comment = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x06) + "test".toByteArray(Charsets.US_ASCII)
        return byteArrayOf(0xFF.toByte(), 0xD8.toByte()) + comment + startOfFrame + byteArrayOf(0xFF.toByte(), 0xD9.toByte())
    }

    fun webpBytes(width: Int, height: Int): ByteArray {
        val extendedHeader = byteArrayOf(0, 0, 0, 0) + littleEndian24(width - 1) + littleEndian24(height - 1)
        val chunk = "VP8X".toByteArray(Charsets.US_ASCII) + littleEndian32(extendedHeader.size) + extendedHeader
        val body = "WEBP".toByteArray(Charsets.US_ASCII) + chunk
        return "RIFF".toByteArray(Charsets.US_ASCII) + littleEndian32(body.size) + body
    }

    fun ttfBytes(): ByteArray = byteArrayOf(0x00, 0x01, 0x00, 0x00) + ByteArray(60)

    private fun intBytes(value: Int) = byteArrayOf(
        (value ushr 24).toByte(), (value ushr 16).toByte(), (value ushr 8).toByte(), value.toByte(),
    )

    private fun shortBytes(value: Int) = byteArrayOf((value ushr 8).toByte(), value.toByte())

    private fun littleEndian24(value: Int) = byteArrayOf(value.toByte(), (value ushr 8).toByte(), (value ushr 16).toByte())

    private fun littleEndian32(value: Int) = littleEndian24(value) + byteArrayOf((value ushr 24).toByte())
}
