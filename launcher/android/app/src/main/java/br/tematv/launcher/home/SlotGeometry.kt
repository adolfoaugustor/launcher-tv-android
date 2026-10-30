package br.tematv.launcher.home

import br.tematv.themecore.FrameSlot

data class PixelRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

/** Geometria das áreas da moldura (`icon_slot`, `label_slot`) dentro de um item da grade. */
object SlotGeometry {

    fun slotRect(slot: FrameSlot, tileWidth: Float, tileHeight: Float) = PixelRect(
        left = (slot.x * tileWidth).toFloat(),
        top = (slot.y * tileHeight).toFloat(),
        right = ((slot.x + slot.w) * tileWidth).toFloat(),
        bottom = ((slot.y + slot.h) * tileHeight).toFloat(),
    )

    /** Maior retângulo com a proporção do conteúdo que cabe em [bounds], centralizado ("cabe sem distorcer"). */
    fun fitInside(contentWidth: Float, contentHeight: Float, bounds: PixelRect): PixelRect {
        val scale = minOf(bounds.width / contentWidth, bounds.height / contentHeight)
        val fittedWidth = contentWidth * scale
        val fittedHeight = contentHeight * scale
        val left = bounds.left + (bounds.width - fittedWidth) / 2
        val top = bounds.top + (bounds.height - fittedHeight) / 2
        return PixelRect(left, top, left + fittedWidth, top + fittedHeight)
    }
}
