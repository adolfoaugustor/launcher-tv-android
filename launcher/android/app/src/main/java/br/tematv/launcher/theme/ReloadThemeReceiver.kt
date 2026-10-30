package br.tematv.launcher.theme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.tematv.launcher.TemaTvApplication
import kotlinx.coroutines.launch

/** Recarrega o tema quando recebe `br.tematv.launcher.action.RELOAD_THEME` (usado pelo ADB na instalação). */
class ReloadThemeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_RELOAD_THEME) return
        val application = context.applicationContext as TemaTvApplication
        val pendingResult = goAsync()
        application.applicationScope.launch {
            try {
                application.themeRepository.reload()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_RELOAD_THEME = "br.tematv.launcher.action.RELOAD_THEME"
    }
}
