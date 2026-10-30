package br.tematv.launcher.activation

import android.content.Context
import br.tematv.activation.AttemptsState
import java.time.Instant

/** Ativação e tentativas em `activation_prefs` (D-07). */
class ActivationStore(context: Context) {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    val activationCode: String? get() = preferences.getString(KEY_ACTIVATION_CODE, null)
    val activatedAt: String? get() = preferences.getString(KEY_ACTIVATED_AT, null)

    val attempts: AttemptsState
        get() = AttemptsState(
            failedAttempts = preferences.getInt(KEY_FAILED_ATTEMPTS, 0),
            lockedUntilElapsedMs = preferences.getLong(KEY_LOCKED_UNTIL, 0),
        )

    fun saveAttempts(state: AttemptsState) {
        preferences.edit()
            .putInt(KEY_FAILED_ATTEMPTS, state.failedAttempts)
            .putLong(KEY_LOCKED_UNTIL, state.lockedUntilElapsedMs)
            .commit()
    }

    fun saveActivation(activationCode: String, at: Instant) {
        preferences.edit()
            .putString(KEY_ACTIVATION_CODE, activationCode)
            .putString(KEY_ACTIVATED_AT, at.toString())
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKED_UNTIL, 0)
            .commit()
    }

    private companion object {
        const val PREFERENCES_NAME = "activation_prefs"
        const val KEY_ACTIVATION_CODE = "activation_code"
        const val KEY_ACTIVATED_AT = "activated_at"
        const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        const val KEY_LOCKED_UNTIL = "locked_until_elapsed_ms"
    }
}
