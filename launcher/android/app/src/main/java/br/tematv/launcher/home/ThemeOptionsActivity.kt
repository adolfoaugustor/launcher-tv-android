package br.tematv.launcher.home

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import br.tematv.launcher.R
import br.tematv.launcher.TemaTvApplication
import br.tematv.launcher.theme.ThemeOrigin

/**
 * Tela "Tema": opções do launcher e diagnóstico (RF-11, RF-12).
 * A troca da tela inicial (feature 3) e a licença (feature 4) ainda não existem (RN-05).
 */
class ThemeOptionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_options)
    }

    override fun onResume() {
        super.onResume()
        showDiagnostics()
    }

    private fun showDiagnostics() {
        val themeRepository = (application as TemaTvApplication).themeRepository
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val activeTheme = themeRepository.activeTheme.value
        val futureVersion = getString(R.string.diagnostics_future_version)

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
        setValue(R.id.takeoverValue, futureVersion)
        setValue(R.id.accessibilityValue, futureVersion)
        setValue(R.id.licenseValue, futureVersion)
    }

    private fun setValue(viewId: Int, text: String) {
        findViewById<TextView>(viewId).text = text
    }
}
