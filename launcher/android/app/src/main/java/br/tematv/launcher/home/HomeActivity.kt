package br.tematv.launcher.home

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.tematv.launcher.R
import br.tematv.launcher.TemaTvApplication
import br.tematv.launcher.theme.ActiveTheme
import br.tematv.launcher.theme.ThemeImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate

/** Tela inicial temática: cabeçalho, versículo do dia e grade de apps dentro da moldura do tema. */
class HomeActivity : ComponentActivity() {

    private class ThemeImages(val background: Bitmap?, val logo: Bitmap, val frame: Bitmap, val typeface: Typeface)

    private lateinit var homeRoot: View
    private lateinit var backgroundImage: ImageView
    private lateinit var logoImage: ImageView
    private lateinit var messageText: TextView
    private lateinit var settingsButton: Button
    private lateinit var themeButton: Button
    private lateinit var verseText: TextView
    private lateinit var verseReferenceText: TextView
    private lateinit var appGrid: RecyclerView
    private lateinit var emptyGridText: TextView
    private lateinit var noticeText: TextView

    private val themeRepository get() = (application as TemaTvApplication).themeRepository
    private val appsSource by lazy { InstalledAppsSource(this) }
    private val launcherPreferences by lazy { getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE) }
    private val gridAdapter = AppGridAdapter(::openApp)
    private var activeTheme: ActiveTheme? = null
    private var gridReceivedInitialFocus = false
    private val hideNotice = Runnable { noticeText.visibility = View.GONE }

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = reloadApps()
    }

    private val dateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = showVerseOfTheDay()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        homeRoot = findViewById(R.id.homeRoot)
        backgroundImage = findViewById(R.id.backgroundImage)
        logoImage = findViewById(R.id.logoImage)
        messageText = findViewById(R.id.messageText)
        settingsButton = findViewById(R.id.settingsButton)
        themeButton = findViewById(R.id.themeButton)
        verseText = findViewById(R.id.verseText)
        verseReferenceText = findViewById(R.id.verseReferenceText)
        appGrid = findViewById(R.id.appGrid)
        emptyGridText = findViewById(R.id.emptyGridText)
        noticeText = findViewById(R.id.noticeText)

        appGrid.layoutManager = GridLayoutManager(this, GRID_COLUMNS)
        appGrid.adapter = gridAdapter
        appGrid.itemAnimator = null

        settingsButton.setOnClickListener { startSafely(Intent(Settings.ACTION_SETTINGS), getString(R.string.button_settings)) }
        themeButton.setOnClickListener { startActivity(Intent(this, ThemeOptionsActivity::class.java)) }

        // Voltar nunca tira o usuário da tela inicial (RF-14).
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeRepository.activeTheme.filterNotNull().collect { applyTheme(it) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerSystemReceiver(packageChangeReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        })
        registerSystemReceiver(dateChangeReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        })
        reloadApps()
        showVerseOfTheDay()
    }

    override fun onStop() {
        unregisterReceiver(packageChangeReceiver)
        unregisterReceiver(dateChangeReceiver)
        super.onStop()
    }

    private fun registerSystemReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    private suspend fun applyTheme(theme: ActiveTheme) {
        val validatedTheme = theme.theme
        val manifest = validatedTheme.manifest
        val images = try {
            withContext(Dispatchers.IO) {
                ThemeImages(
                    background = validatedTheme.backgroundFile?.let { ThemeImageLoader.decodeBitmap(it) },
                    logo = ThemeImageLoader.decodeBitmap(validatedTheme.logoFile, LOGO_MAX_PX, LOGO_MAX_PX),
                    frame = ThemeImageLoader.decodeBitmap(validatedTheme.iconFrameFile, FRAME_MAX_WIDTH_PX, FRAME_MAX_HEIGHT_PX),
                    typeface = ThemeImageLoader.loadTypeface(validatedTheme.fontFile),
                )
            }
        } catch (exception: IOException) {
            Log.w(LOG_TAG, "Imagens do tema ${manifest.themeId} ilegíveis; tela mantida: ${exception.message}")
            return
        }
        activeTheme = theme

        val backgroundColor = Color.parseColor(manifest.colors.background)
        val textColor = Color.parseColor(manifest.colors.text)
        val accentColor = Color.parseColor(manifest.colors.accent)

        homeRoot.setBackgroundColor(backgroundColor)
        backgroundImage.setImageBitmap(images.background)
        logoImage.setImageBitmap(images.logo)
        messageText.text = manifest.message
        listOf(messageText, verseText, verseReferenceText, emptyGridText).forEach {
            it.setTextColor(textColor)
            it.typeface = images.typeface
        }
        styleHeaderButton(settingsButton, backgroundColor, textColor, accentColor, images.typeface)
        styleHeaderButton(themeButton, backgroundColor, textColor, accentColor, images.typeface)
        gridAdapter.style = TileStyle(
            frame = images.frame,
            iconSlot = manifest.iconFrame.iconSlot,
            labelSlot = manifest.iconFrame.labelSlot,
            textColor = textColor,
            accentColor = accentColor,
            typeface = images.typeface,
        )
        showVerseOfTheDay()
    }

    private fun styleHeaderButton(button: Button, backgroundColor: Int, textColor: Int, accentColor: Int, typeface: Typeface) {
        val radius = BUTTON_RADIUS_DP * resources.displayMetrics.density
        val strokeWidth = (BUTTON_STROKE_DP * resources.displayMetrics.density).toInt()
        val focused = GradientDrawable().apply {
            cornerRadius = radius
            setColor(accentColor)
        }
        val normal = GradientDrawable().apply {
            cornerRadius = radius
            setColor(backgroundColor)
            setStroke(strokeWidth, accentColor)
        }
        button.background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(), normal)
        }
        button.setTextColor(
            android.content.res.ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(backgroundColor, textColor),
            ),
        )
        button.typeface = typeface
    }

    private fun showVerseOfTheDay() {
        val manifest = activeTheme?.theme?.manifest ?: return
        val verse = manifest.verses[VerseOfTheDay.indexFor(LocalDate.now(), manifest.verses.size)]
        verseText.text = verse.text
        verseReferenceText.text = getString(R.string.verse_reference, verse.reference, verse.version, manifest.versesAttribution)
    }

    private fun reloadApps() {
        lifecycleScope.launch {
            val focusedPosition = appGrid.focusedChild?.let { appGrid.getChildAdapterPosition(it) } ?: RecyclerView.NO_POSITION
            val focusedPackage = gridAdapter.entries.getOrNull(focusedPosition)?.packageName
            val gridHadFocus = focusedPosition != RecyclerView.NO_POSITION

            val entries = withContext(Dispatchers.IO) { appsSource.load() }
            gridAdapter.submit(entries)
            emptyGridText.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE

            if (entries.isEmpty()) {
                settingsButton.requestFocus()
                return@launch
            }
            // Na primeira carga o foco vai para a grade, mesmo que um botão do cabeçalho já o tenha pegado.
            val isInitialLoad = !gridReceivedInitialFocus
            if (!gridHadFocus && !isInitialLoad) return@launch
            gridReceivedInitialFocus = true

            val targetPosition = when {
                focusedPackage != null && gridAdapter.positionOf(focusedPackage) >= 0 -> gridAdapter.positionOf(focusedPackage)
                // App em foco foi removido: o foco vai para o item seguinte, que ocupou a mesma posição (EC-01).
                focusedPackage != null -> focusedPosition.coerceAtMost(entries.lastIndex)
                else -> launcherPreferences.getString(KEY_LAST_FOCUSED_PACKAGE, null)
                    ?.let(gridAdapter::positionOf)?.takeIf { it >= 0 } ?: 0
            }
            focusGridPosition(targetPosition)
        }
    }

    private fun focusGridPosition(position: Int) {
        appGrid.scrollToPosition(position)
        appGrid.post { appGrid.layoutManager?.findViewByPosition(position)?.requestFocus() }
    }

    private fun openApp(entry: AppEntry) {
        val launchIntent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            .setClassName(entry.app.packageName, entry.app.activityName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        if (startSafely(launchIntent, entry.label)) {
            launcherPreferences.edit().putString(KEY_LAST_FOCUSED_PACKAGE, entry.packageName).apply()
        }
    }

    /** Abre a tela pedida; em caso de falha mostra o aviso por 4 s e registra no log (EC-02). */
    private fun startSafely(intent: Intent, targetName: String): Boolean = try {
        startActivity(intent)
        true
    } catch (exception: ActivityNotFoundException) {
        reportOpenFailure(intent, targetName, exception)
    } catch (exception: SecurityException) {
        reportOpenFailure(intent, targetName, exception)
    }

    private fun reportOpenFailure(intent: Intent, targetName: String, exception: Exception): Boolean {
        Log.w(LOG_TAG, "Falha ao abrir ${intent.component?.packageName ?: intent.action}: ${exception.message}")
        noticeText.text = getString(R.string.app_open_failed, targetName)
        noticeText.visibility = View.VISIBLE
        noticeText.removeCallbacks(hideNotice)
        noticeText.postDelayed(hideNotice, NOTICE_DURATION_MS)
        return false
    }

    private companion object {
        const val LOG_TAG = "TemaTV"
        const val PREFERENCES_NAME = "launcher_prefs"
        const val KEY_LAST_FOCUSED_PACKAGE = "last_focused_package"
        const val GRID_COLUMNS = 5
        const val NOTICE_DURATION_MS = 4_000L
        const val LOGO_MAX_PX = 480
        const val FRAME_MAX_WIDTH_PX = 520
        const val FRAME_MAX_HEIGHT_PX = 680
        const val BUTTON_RADIUS_DP = 20f
        const val BUTTON_STROKE_DP = 2f
    }
}
