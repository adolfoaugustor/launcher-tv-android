package br.tematv.launcher.takeover

import android.content.Context
import java.time.Instant

/** Preferência e datas da troca em `takeover_prefs` (D-08). Instantes de janela em `elapsedRealtime`. */
class TakeoverPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    val isTakeoverEnabled: Boolean get() = preferences.getBoolean(KEY_TAKEOVER_ENABLED, true)
    val lastTakeoverAt: String? get() = preferences.getString(KEY_LAST_TAKEOVER_AT, null)
    val lastServiceOffAt: String? get() = preferences.getString(KEY_LAST_SERVICE_OFF_AT, null)
    val lastLoopPauseAt: String? get() = preferences.getString(KEY_LAST_LOOP_PAUSE_AT, null)

    fun snapshot(recentOpensElapsedMs: List<Long>) = TakeoverState(
        takeoverEnabled = isTakeoverEnabled,
        suppressUntilElapsedMs = preferences.getLong(KEY_SUPPRESS_UNTIL, 0),
        pausedUntilElapsedMs = preferences.getLong(KEY_PAUSED_UNTIL, 0),
        recentOpensElapsedMs = recentOpensElapsedMs,
    )

    /** Desliga a troca e ignora o launcher do sistema pelos próximos 5 s (RF-06, RF-08). */
    fun disableTakeover(nowElapsedMs: Long) {
        preferences.edit()
            .putBoolean(KEY_TAKEOVER_ENABLED, false)
            .putLong(KEY_SUPPRESS_UNTIL, nowElapsedMs + TakeoverPolicy.SUPPRESS_AFTER_DISABLE_MS)
            .commit()
    }

    fun enableTakeover() {
        preferences.edit()
            .putBoolean(KEY_TAKEOVER_ENABLED, true)
            .putLong(KEY_SUPPRESS_UNTIL, 0)
            .commit()
    }

    /** Pausa a troca sem mudar `takeover_enabled` (RN-04). */
    fun pauseForLoop(nowElapsedMs: Long, now: Instant) {
        preferences.edit()
            .putLong(KEY_PAUSED_UNTIL, nowElapsedMs + TakeoverPolicy.LOOP_PAUSE_MS)
            .putString(KEY_LAST_LOOP_PAUSE_AT, now.toString())
            .commit()
    }

    /** Depois de uma falha ao abrir a tela temática, não tenta de novo por 10 s (EC-05). */
    fun pauseAfterOpenFailure(nowElapsedMs: Long, now: Instant) {
        preferences.edit()
            .putLong(KEY_PAUSED_UNTIL, nowElapsedMs + TakeoverPolicy.OPEN_FAILURE_RETRY_MS)
            .putString(KEY_LAST_OPEN_FAILURE_AT, now.toString())
            .commit()
    }

    fun recordTakeover(now: Instant) {
        preferences.edit().putString(KEY_LAST_TAKEOVER_AT, now.toString()).apply()
    }

    fun recordServiceOff(now: Instant) {
        preferences.edit().putString(KEY_LAST_SERVICE_OFF_AT, now.toString()).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "takeover_prefs"
        const val KEY_TAKEOVER_ENABLED = "takeover_enabled"
        const val KEY_SUPPRESS_UNTIL = "suppress_until_elapsed_ms"
        const val KEY_PAUSED_UNTIL = "paused_until_elapsed_ms"
        const val KEY_LAST_TAKEOVER_AT = "last_takeover_at"
        const val KEY_LAST_SERVICE_OFF_AT = "last_service_off_at"
        const val KEY_LAST_LOOP_PAUSE_AT = "last_loop_pause_at"
        const val KEY_LAST_OPEN_FAILURE_AT = "last_open_failure_at"
    }
}
