package br.tematv.launcher.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.view.View
import br.tematv.themecore.FrameSlot
import kotlin.math.roundToInt

/** Aparência compartilhada por todos os itens da grade, vinda do tema ativo. */
data class TileStyle(
    val frame: Bitmap,
    val iconSlot: FrameSlot,
    val labelSlot: FrameSlot,
    val textColor: Int,
    val accentColor: Int,
    val typeface: Typeface,
)

/**
 * Um app da grade desenhado numa única View: moldura do tema, banner (ou ícone) no `icon_slot`
 * e nome no `label_slot`. No foco, borda na cor de destaque e zoom de 10% (RF-02, RF-05).
 */
class ThemedAppTileView(context: Context) : View(context) {

    var style: TileStyle? = null
        set(value) {
            field = value
            invalidate()
        }

    var entry: AppEntry? = null
        set(value) {
            field = value
            contentDescription = value?.label
            invalidate()
        }

    private val density = resources.displayMetrics.density
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val focusBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = FOCUS_BORDER_DP * density
    }
    private val frameBounds = RectF()
    private val artworkBounds = Rect()

    init {
        isFocusable = true
        isClickable = true
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        val scale = if (gainFocus) FOCUSED_SCALE else 1f
        animate()
            .scaleX(scale)
            .scaleY(scale)
            .translationZ(if (gainFocus) FOCUSED_ELEVATION_DP * density else 0f)
            .setDuration(FOCUS_ANIMATION_MS)
            .start()
    }

    override fun onDraw(canvas: Canvas) {
        val style = style ?: return
        val tileWidth = width.toFloat()
        val tileHeight = height.toFloat()

        frameBounds.set(0f, 0f, tileWidth, tileHeight)
        canvas.drawBitmap(style.frame, null, frameBounds, bitmapPaint)

        entry?.let { drawApp(canvas, it, style, tileWidth, tileHeight) }

        if (isFocused) {
            focusBorderPaint.color = style.accentColor
            val inset = focusBorderPaint.strokeWidth / 2
            val radius = CORNER_RADIUS_DP * density
            canvas.drawRoundRect(inset, inset, tileWidth - inset, tileHeight - inset, radius, radius, focusBorderPaint)
        }
    }

    private fun drawApp(canvas: Canvas, entry: AppEntry, style: TileStyle, tileWidth: Float, tileHeight: Float) {
        val iconArea = SlotGeometry.slotRect(style.iconSlot, tileWidth, tileHeight)
        val artwork = entry.artwork
        val contentWidth = artwork.intrinsicWidth.takeIf { it > 0 }?.toFloat() ?: iconArea.width
        val contentHeight = artwork.intrinsicHeight.takeIf { it > 0 }?.toFloat() ?: iconArea.height
        val fitted = SlotGeometry.fitInside(contentWidth, contentHeight, iconArea)
        artworkBounds.set(fitted.left.roundToInt(), fitted.top.roundToInt(), fitted.right.roundToInt(), fitted.bottom.roundToInt())
        artwork.bounds = artworkBounds
        artwork.draw(canvas)

        val labelArea = SlotGeometry.slotRect(style.labelSlot, tileWidth, tileHeight)
        labelPaint.color = style.textColor
        labelPaint.typeface = style.typeface
        labelPaint.textSize = maxOf(MIN_LABEL_TEXT_PX, labelArea.height * LABEL_HEIGHT_RATIO)
        val label = TextUtils.ellipsize(entry.label, labelPaint, labelArea.width * LABEL_WIDTH_RATIO, TextUtils.TruncateAt.END)
        val baseline = labelArea.top + labelArea.height / 2 - (labelPaint.descent() + labelPaint.ascent()) / 2
        canvas.drawText(label, 0, label.length, labelArea.left + labelArea.width / 2, baseline, labelPaint)
    }

    private companion object {
        const val FOCUSED_SCALE = 1.1f
        const val FOCUSED_ELEVATION_DP = 8f
        const val FOCUS_ANIMATION_MS = 120L
        const val FOCUS_BORDER_DP = 3f
        const val CORNER_RADIUS_DP = 10f
        const val MIN_LABEL_TEXT_PX = 20f
        const val LABEL_HEIGHT_RATIO = 0.5f
        const val LABEL_WIDTH_RATIO = 0.92f
    }
}
