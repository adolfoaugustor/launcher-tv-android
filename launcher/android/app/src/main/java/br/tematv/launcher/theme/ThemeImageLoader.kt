package br.tematv.launcher.theme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import java.io.File
import java.io.IOException

/** Carrega as imagens e a fonte do tema sem estourar a memória da TV de 2 GB. */
object ThemeImageLoader {

    const val MAX_WIDTH_PX = 1920
    const val MAX_HEIGHT_PX = 1080

    fun decodeBitmap(file: File, maxWidthPx: Int = MAX_WIDTH_PX, maxHeightPx: Int = MAX_HEIGHT_PX): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("imagem ilegível: $file")

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxWidthPx, maxHeightPx)
        }
        return BitmapFactory.decodeFile(file.path, options) ?: throw IOException("imagem ilegível: $file")
    }

    /** Menor potência de 2 que deixa a imagem dentro de [maxWidthPx] × [maxHeightPx]. */
    fun sampleSizeFor(widthPx: Int, heightPx: Int, maxWidthPx: Int, maxHeightPx: Int): Int {
        var sampleSize = 1
        while (widthPx / sampleSize > maxWidthPx || heightPx / sampleSize > maxHeightPx) sampleSize *= 2
        return sampleSize
    }

    fun loadTypeface(fontFile: File?): Typeface = fontFile?.let { Typeface.createFromFile(it) } ?: Typeface.DEFAULT
}
