package br.tematv.themebuilder

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object TestImages {
    fun png(width: Int, height: Int): ByteArray = encode(BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png")

    fun jpeg(width: Int, height: Int): ByteArray = encode(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "jpg")

    /** Só o cabeçalho VP8X: basta para o `ImageHeaderReader` ler as dimensões. */
    fun webpHeader(width: Int, height: Int): ByteArray {
        val bytes = ByteArray(30)
        "RIFF".toByteArray().copyInto(bytes, 0)
        "WEBPVP8X".toByteArray().copyInto(bytes, 8)
        fun put24(offset: Int, value: Int) {
            bytes[offset] = value.toByte()
            bytes[offset + 1] = (value shr 8).toByte()
            bytes[offset + 2] = (value shr 16).toByte()
        }
        put24(24, width - 1)
        put24(27, height - 1)
        return bytes
    }

    private fun encode(image: BufferedImage, format: String): ByteArray =
        ByteArrayOutputStream().also { ImageIO.write(image, format, it) }.toByteArray()
}
