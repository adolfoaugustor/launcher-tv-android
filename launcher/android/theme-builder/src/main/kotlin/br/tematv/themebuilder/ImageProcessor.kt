package br.tematv.themebuilder

import br.tematv.themecore.ImageFormat
import br.tematv.themecore.ImageHeaderReader
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.math.roundToInt

enum class ImageKind(val field: String, val baseName: String, val allowedFormats: Set<ImageFormat>) {
    LOGO("logo", "logo", setOf(ImageFormat.PNG)),
    BACKGROUND("fundo", "fundo", setOf(ImageFormat.PNG, ImageFormat.JPEG, ImageFormat.WEBP)),
    FRAME("moldura", "moldura", setOf(ImageFormat.PNG)),
}

/** Confere e grava as imagens do projeto, reduzindo PNG e JPEG maiores que a tela (D-07, RF-09). */
class ImageProcessor {

    /** Grava a imagem na pasta do projeto e devolve o nome do arquivo (`logo.png`, `fundo.jpg`...). */
    fun save(kind: ImageKind, bytes: ByteArray, projectDir: File): String {
        if (kind == ImageKind.LOGO && bytes.size > MAX_LOGO_BYTES) {
            throw UserInputException(kind.field, "Envie um PNG de até 5 MB.")
        }
        val upload = File.createTempFile("envio-", ".img", projectDir)
        try {
            upload.writeBytes(bytes)
            val info = ImageHeaderReader.read(upload)
            if (info == null || info.format !in kind.allowedFormats) {
                throw UserInputException(kind.field, formatMessage(kind))
            }
            val fileName = "${kind.baseName}.${extension(info.format)}"
            val target = File(projectDir, fileName)
            if (info.width > MAX_WIDTH || info.height > MAX_HEIGHT) {
                if (info.format == ImageFormat.WEBP) {
                    throw UserInputException(kind.field, "Imagem WEBP maior que 1920×1080: envie em PNG ou JPEG para a ferramenta reduzir.")
                }
                writeReduced(upload, info.format, target)
            } else {
                upload.copyTo(target, overwrite = true)
            }
            projectDir.listFiles { file -> file.name.startsWith("${kind.baseName}.") && file.name != fileName }
                ?.forEach { it.delete() }
            return fileName
        } finally {
            upload.delete()
        }
    }

    private fun writeReduced(source: File, format: ImageFormat, target: File) {
        val original = ImageIO.read(source) ?: throw UserInputException(null, "Não foi possível ler a imagem enviada.")
        val scale = min(MAX_WIDTH.toDouble() / original.width, MAX_HEIGHT.toDouble() / original.height)
        val width = (original.width * scale).roundToInt().coerceAtLeast(1)
        val height = (original.height * scale).roundToInt().coerceAtLeast(1)
        val imageType = if (format == ImageFormat.PNG) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
        val reduced = BufferedImage(width, height, imageType)
        reduced.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            drawImage(original, 0, 0, width, height, null)
            dispose()
        }
        val formatName = if (format == ImageFormat.PNG) "png" else "jpg"
        check(ImageIO.write(reduced, formatName, target)) { "ImageIO sem gravador para $formatName" }
    }

    private fun formatMessage(kind: ImageKind): String = when (kind) {
        ImageKind.LOGO -> "Envie um PNG de até 5 MB."
        ImageKind.BACKGROUND -> "Envie o fundo em PNG, JPEG ou WEBP."
        ImageKind.FRAME -> "Envie a moldura em PNG."
    }

    private fun extension(format: ImageFormat): String = when (format) {
        ImageFormat.PNG -> "png"
        ImageFormat.JPEG -> "jpg"
        ImageFormat.WEBP -> "webp"
    }

    companion object {
        const val MAX_WIDTH = 1920
        const val MAX_HEIGHT = 1080
        const val MAX_LOGO_BYTES = 5 * 1024 * 1024
    }
}
