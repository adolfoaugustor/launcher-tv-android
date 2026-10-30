package br.tematv.launcher.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import br.tematv.launcher.R
import br.tematv.launcher.TemaTvApplication
import br.tematv.launcher.takeover.AccessibilityServiceState
import br.tematv.launcher.takeover.HoldToConfirm
import br.tematv.launcher.takeover.TakeoverPreferences
import br.tematv.launcher.theme.ThemeOrigin

/**
 * Tela "Tema": ligar e desligar a troca da tela inicial e diagnóstico (RF-06, RF-07, RF-10, RF-11, RF-13).
 * Desligar exige segurar OK por 3 s. A licença (feature 004) ainda não existe (RN-05).
 */
class ThemeOptionsActivity : ComponentActivity() {

    private lateinit var takeoverPreferences: TakeoverPreferences
    private lateinit var takeoverToggleButton: Button
    private lateinit var holdProgress: ProgressBar
    private lateinit var holdHintText: TextView
    private val holdToConfirm = HoldToConfirm()
    private var ignoreKeysUntilRelease = false

    private val holdTicker = object : Runnable {
        override fun run() {
            val now = SystemClock.uptimeMillis()
            holdProgress.progress = (holdToConfirm.progress(now) * PROGRESS_MAX).toInt()
            if (holdToConfirm.isConfirmed(now)) {
                holdToConfirm.release()
                ignoreKeysUntilRelease = true
                disableTakeover()
            } else if (holdToConfirm.isHolding) {
                holdProgress.postDelayed(this, HOLD_TICK_MS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_options)
        takeoverPreferences = TakeoverPreferences(this)
        takeoverToggleButton = findViewById(R.id.takeoverToggleButton)
        holdProgress = findViewById(R.id.holdProgress)
        holdHintText = findViewById(R.id.holdHintText)

        takeoverToggleButton.setOnKeyListener { _, keyCode, event -> handleToggleKey(keyCode, event) }
        takeoverToggleButton.setOnClickListener {
            if (!takeoverPreferences.isTakeoverEnabled) enableTakeover()
        }
        findViewById<Button>(R.id.openAccessibilitySettingsButton).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (exception: ActivityNotFoundException) {
                Log.w(LOG_TAG, "Configurações de acessibilidade indisponíveis: ${exception.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showTakeoverOptions()
        showDiagnostics()
        takeoverToggleButton.requestFocus()
    }

    override fun onPause() {
        cancelHold()
        super.onPause()
    }

    /** Com a troca ligada, OK precisa ficar apertado 3 s; um toque curto só mostra a dica (RF-13). */
    private fun handleToggleKey(keyCode: Int, event: KeyEvent): Boolean {
        val isConfirmKey = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
        // Depois de desligar, o OK ainda está apertado: o soltar não pode virar clique que religa a troca.
        if (isConfirmKey && ignoreKeysUntilRelease) {
            if (event.action == KeyEvent.ACTION_UP) ignoreKeysUntilRelease = false
            return true
        }
        if (!isConfirmKey || !takeoverPreferences.isTakeoverEnabled) return false
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (!holdToConfirm.isHolding) {
                holdToConfirm.press(SystemClock.uptimeMillis())
                holdHintText.visibility = View.GONE
                holdProgress.progress = 0
                holdProgress.visibility = View.VISIBLE
                holdProgress.post(holdTicker)
            }
            KeyEvent.ACTION_UP -> if (holdToConfirm.isHolding) {
                cancelHold()
                holdHintText.visibility = View.VISIBLE
            }
        }
        return true
    }

    private fun cancelHold() {
        holdToConfirm.release()
        holdProgress.removeCallbacks(holdTicker)
        holdProgress.visibility = View.GONE
    }

    private fun disableTakeover() {
        holdProgress.visibility = View.GONE
        takeoverPreferences.disableTakeover(SystemClock.elapsedRealtime())
        Log.i(LOG_TAG, "Troca da tela inicial: desligada pelo usuário")
        // Resolve para o launcher do sistema, que tem prioridade sobre apps comuns na categoria HOME (D-12).
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    private fun enableTakeover() {
        takeoverPreferences.enableTakeover()
        Log.i(LOG_TAG, "Troca da tela inicial: ligada pelo usuário")
        showTakeoverOptions()
        showDiagnostics()
    }

    private fun showTakeoverOptions() {
        val takeoverEnabled = takeoverPreferences.isTakeoverEnabled
        val serviceEnabled = AccessibilityServiceState.isServiceEnabled(this)
        setValue(R.id.takeoverValue, getString(if (takeoverEnabled) R.string.diagnostics_state_on else R.string.diagnostics_state_off))
        takeoverToggleButton.text = getString(if (takeoverEnabled) R.string.takeover_disable else R.string.takeover_enable)
        holdHintText.visibility = View.GONE
        setValue(R.id.accessibilityValue, getString(if (serviceEnabled) R.string.diagnostics_service_on else R.string.diagnostics_service_off))
        val showSteps = takeoverEnabled && !serviceEnabled
        findViewById<View>(R.id.accessibilityStepsText).visibility = if (showSteps) View.VISIBLE else View.GONE
        findViewById<View>(R.id.openAccessibilitySettingsButton).visibility = if (showSteps) View.VISIBLE else View.GONE
    }

    private fun showDiagnostics() {
        val themeRepository = (application as TemaTvApplication).themeRepository
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val activeTheme = themeRepository.activeTheme.value
        val never = getString(R.string.diagnostics_never)

        setValue(R.id.appVersionValue, "${packageInfo.versionName} (${packageInfo.longVersionCode})")
        setValue(
            R.id.activeThemeValue,
            activeTheme?.theme?.manifest?.let { "${it.displayName} (${it.themeId})" } ?: getString(R.string.diagnostics_loading),
        )
        setValue(
            R.id.themeOriginValue,
            when (val origin = activeTheme?.origin) {
                is ThemeOrigin.Package -> getString(R.string.diagnostics_origin_package, origin.packageName)
                ThemeOrigin.Default -> getString(R.string.diagnostics_origin_default)
                null -> getString(R.string.diagnostics_loading)
            },
        )
        setValue(
            R.id.lastErrorValue,
            themeRepository.lastError?.let {
                val codeAndField = listOfNotNull(it.code, it.field).joinToString(" ")
                getString(R.string.diagnostics_error_detail, codeAndField, it.packageName ?: "-", it.recordedAt)
            } ?: getString(R.string.diagnostics_no_error),
        )
        setValue(R.id.lastTakeoverValue, takeoverPreferences.lastTakeoverAt ?: never)
        setValue(R.id.lastServiceOffValue, takeoverPreferences.lastServiceOffAt ?: never)
        setValue(R.id.lastLoopPauseValue, takeoverPreferences.lastLoopPauseAt ?: never)
        setValue(R.id.licenseValue, getString(R.string.diagnostics_future_version))
    }

    private fun setValue(viewId: Int, text: String) {
        findViewById<TextView>(viewId).text = text
    }

    private companion object {
        const val LOG_TAG = "TemaTV"
        const val PROGRESS_MAX = 100
        const val HOLD_TICK_MS = 50L
    }
}
