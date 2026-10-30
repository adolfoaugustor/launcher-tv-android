package br.tematv.themecore

import java.io.DataInputStream
import java.io.EOFException
import java.io.File
import java.io.InputStream

enum class ImageFormat { PNG, JPEG, WEBP }

data class ImageInfo(val format: ImageFormat, val width: Int, val height: Int)

/**
 * Reconhece PNG, JPEG e WEBP pela assinatura e lê largura e altura do cabeçalho,
 * sem decodificar a imagem. Usa só APIs disponíveis no Android 9 (API 28).
 */
object ImageHeaderReader {

    private const val HEADER_BYTES = 30
    private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

    fun read(file: File): ImageInfo? {
        val header = file.inputStream().use { readUpTo(it, HEADER_BYTES) }
        return when {
            header.startsWith(PNG_SIGNATURE) -> readPng(header)
            header.size >= 2 && header.unsigned(0) == 0xFF && header.unsigned(1) == 0xD8 -> readJpeg(file)
            header.ascii(0, 4) == "RIFF" && header.ascii(8, 4) == "WEBP" -> readWebp(header)
            else -> null
        }?.takeIf { it.width > 0 && it.height > 0 }
    }

    private fun readPng(header: ByteArray): ImageInfo? {
        if (header.size < 24 || header.ascii(12, 4) != "IHDR") return null
        return ImageInfo(ImageFormat.PNG, header.bigEndian32(16), header.bigEndian32(20))
    }

    private fun readWebp(header: ByteArray): ImageInfo? {
        if (header.size < HEADER_BYTES) return null
        return when (header.ascii(12, 4)) {
            "VP8 " -> ImageInfo(
                ImageFormat.WEBP,
                header.littleEndian16(26) and 0x3FFF,
                header.littleEndian16(28) and 0x3FFF,
            )
            "VP8L" -> {
                val b1 = header.unsigned(21)
                val b2 = header.unsigned(22)
                val b3 = header.unsigned(23)
                val b4 = header.unsigned(24)
                ImageInfo(
                    ImageFormat.WEBP,
                    1 + (b1 or ((b2 and 0x3F) shl 8)),
                    1 + (((b2 and 0xC0) shr 6) or (b3 shl 2) or ((b4 and 0x0F) shl 10)),
                )
            }
            "VP8X" -> ImageInfo(ImageFormat.WEBP, 1 + header.littleEndian24(24), 1 + header.littleEndian24(27))
            else -> null
        }
    }

    private fun readJpeg(file: File): ImageInfo? = DataInputStream(file.inputStream().buffered()).use { input ->
        try {
            findJpegStartOfFrame(input)
        } catch (exception: EOFException) {
            null
        }
    }

    /** Percorre os segmentos do JPEG até o marcador SOF, que traz altura e largura. */
    private fun findJpegStartOfFrame(input: DataInputStream): ImageInfo? {
        input.skipFully(2)
        while (true) {
            if (input.readUnsignedByte() != 0xFF) return null
            var marker = input.readUnsignedByte()
            while (marker == 0xFF) marker = input.readUnsignedByte()
            if (marker == 0xD9 || marker == 0xDA) return null
            if (marker in 0xD0..0xD7 || marker == 0x01) continue

            val segmentLength = input.readUnsignedShort()
            if (segmentLength < 2) return null
            val isStartOfFrame = marker in 0xC0..0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC
            if (isStartOfFrame) {
                input.skipFully(1)
                val height = input.readUnsignedShort()
                val width = input.readUnsignedShort()
                return ImageInfo(ImageFormat.JPEG, width, height)
            }
            input.skipFully(segmentLength - 2)
        }
    }

    private fun readUpTo(input: InputStream, count: Int): ByteArray {
        val buffer = ByteArray(count)
        var filled = 0
        while (filled < count) {
            val read = input.read(buffer, filled, count - filled)
            if (read < 0) break
            filled += read
        }
        return buffer.copyOf(filled)
    }

    private fun DataInputStream.skipFully(count: Int) {
        var remaining = count
        while (remaining > 0) {
            val skipped = skipBytes(remaining)
            if (skipped <= 0) {
                readUnsignedByte()
                remaining -= 1
            } else {
                remaining -= skipped
            }
        }
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

    private fun ByteArray.unsigned(index: Int): Int = this[index].toInt() and 0xFF

    private fun ByteArray.ascii(offset: Int, length: Int): String? =
        if (size < offset + length) null else String(this, offset, length, Charsets.US_ASCII)

    private fun ByteArray.bigEndian32(offset: Int): Int =
        (unsigned(offset) shl 24) or (unsigned(offset + 1) shl 16) or (unsigned(offset + 2) shl 8) or unsigned(offset + 3)

    private fun ByteArray.littleEndian16(offset: Int): Int = unsigned(offset) or (unsigned(offset + 1) shl 8)

    private fun ByteArray.littleEndian24(offset: Int): Int =
        unsigned(offset) or (unsigned(offset + 1) shl 8) or (unsigned(offset + 2) shl 16)
}
