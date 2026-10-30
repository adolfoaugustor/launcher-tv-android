package br.tematv.activation

/** Erros seguidos e fim da espera, em `elapsedRealtime`. */
data class AttemptsState(val failedAttempts: Int, val lockedUntilElapsedMs: Long)

/** Depois de [MAX_FAILURES] senhas erradas seguidas, espera [LOCK_MS] antes de aceitar outra (RN-05). */
object ActivationAttempts {
    const val MAX_FAILURES = 5
    const val LOCK_MS = 30_000L

    val NONE = AttemptsState(failedAttempts = 0, lockedUntilElapsedMs = 0)

    /** Espera restante; valores de antes de a TV reiniciar (muito à frente do relógio) contam como expirados. */
    fun remainingLockMs(state: AttemptsState, nowElapsedMs: Long): Long {
        val remaining = state.lockedUntilElapsedMs - nowElapsedMs
        return if (remaining in 1..LOCK_MS) remaining else 0
    }

    fun afterFailure(state: AttemptsState, nowElapsedMs: Long): AttemptsState {
        val failures = state.failedAttempts + 1
        return if (failures >= MAX_FAILURES) AttemptsState(0, nowElapsedMs + LOCK_MS) else AttemptsState(failures, 0)
    }
}
