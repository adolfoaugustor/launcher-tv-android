package br.tematv.launcher.takeover

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import br.tematv.launcher.theme.ActiveTheme
import br.tematv.launcher.theme.ThemeImageLoader
import java.io.File
import java.io.IOException

/**
 * Capa do tema (RF-14, D-16): janela de sobreposição de acessibilidade com o fundo e o logotipo do tema ativo,
 * mostrada na hora por cima da tela do Google enquanto o Android segura a abertura da tela temática após o Home.
 * Não é focável nem tocável; some quando a tela temática ganha foco ou após [MAX_VISIBLE_MS].
 */
class ThemeCover(private val context: Context) {

    private class CoverImages(val themeRootDir: File, val background: Bitmap?, val logo: Bitmap)

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var coverView: View? = null
    private var cachedImages: CoverImages? = null
    private val hideAfterTimeout = Runnable { hide() }

    fun show(activeTheme: ActiveTheme?) {
        if (coverView != null) return
        val view = buildView(activeTheme)
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.OPAQUE,
        )
        windowManager.addView(view, layoutParams)
        coverView = view
        view.postDelayed(hideAfterTimeout, MAX_VISIBLE_MS)
    }

    fun hide() {
        val view = coverView ?: return
        view.removeCallbacks(hideAfterTimeout)
        windowManager.removeView(view)
        coverView = null
    }

    private fun buildView(activeTheme: ActiveTheme?): View {
        val manifest = activeTheme?.theme?.manifest
        val root = FrameLayout(context).apply {
            setBackgroundColor(manifest?.let { Color.parseColor(it.colors.background) } ?: DEFAULT_BACKGROUND)
        }
        val images = activeTheme?.let { imagesFor(it) } ?: return root
        images.background?.let { background ->
            root.addView(ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(background)
            })
        }
        root.addView(
            ImageView(context).apply { setImageBitmap(images.logo) },
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER),
        )
        return root
    }

    /** As imagens são decodificadas uma vez por tema e reaproveitadas a cada Home. */
    private fun imagesFor(activeTheme: ActiveTheme): CoverImages? {
        val theme = activeTheme.theme
        cachedImages?.takeIf { it.themeRootDir == theme.rootDir }?.let { return it }
        return try {
            CoverImages(
                themeRootDir = theme.rootDir,
                background = theme.backgroundFile?.let { ThemeImageLoader.decodeBitmap(it) },
                logo = ThemeImageLoader.decodeBitmap(theme.logoFile, LOGO_MAX_PX, LOGO_MAX_PX),
            ).also { cachedImages = it }
        } catch (exception: IOException) {
            Log.w(LOG_TAG, "Capa do tema sem imagens: ${exception.message}")
            null
        }
    }

    private companion object {
        const val LOG_TAG = "TemaTV"
        const val MAX_VISIBLE_MS = 8_000L
        const val LOGO_MAX_PX = 480
        val DEFAULT_BACKGROUND = Color.parseColor("#FFF6E0")
    }
}
