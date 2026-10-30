package br.tematv.launcher.takeover

/** Estado lido das preferências mais o histórico de aberturas do serviço; instantes em `elapsedRealtime`. */
data class TakeoverState(
    val takeoverEnabled: Boolean,
    val suppressUntilElapsedMs: Long,
    val pausedUntilElapsedMs: Long,
    val recentOpensElapsedMs: List<Long>,
)

enum class IgnoreReason {
    NOT_SYSTEM_LAUNCHER,
    TAKEOVER_DISABLED,
    SUPPRESSED_AFTER_DISABLE,
    PAUSED,
    LICENSE_INVALID,
    RECENTLY_OPENED,
}

sealed interface TakeoverDecision {
    data object Open : TakeoverDecision
    data object PauseForLoop : TakeoverDecision
    data class Ignore(val reason: IgnoreReason) : TakeoverDecision
}

/**
 * Decide se a tela temática deve ser aberta quando um launcher do sistema aparece (D-03).
 * Janelas de tempo: 5 s após desligar (RF-08); no máximo [maxOpensInWindow] aberturas em [loopWindowMs],
 * acima disso pausa [loopPauseMs] sem mexer na preferência (RF-09, RN-04). Eventos repetidos do mesmo
 * Home chegam em rajada e são agrupados por [minIntervalBetweenOpensMs], sem contar como nova abertura.
 */
class TakeoverPolicy(
    private val launcherPackages: Set<String> = SystemLauncherPackages.packageNames,
    private val maxOpensInWindow: Int = 3,
    private val loopWindowMs: Long = 10_000,
    private val minIntervalBetweenOpensMs: Long = 1_500,
) {

    fun decide(packageName: String?, state: TakeoverState, licenseValid: Boolean, nowElapsedMs: Long): TakeoverDecision {
        if (packageName !in launcherPackages) return TakeoverDecision.Ignore(IgnoreReason.NOT_SYSTEM_LAUNCHER)
        return decideForSystemLauncher(state, licenseValid, nowElapsedMs)
    }

    /** Logo após o boot o launcher do sistema já está na tela, sem evento de janela (D-05). */
    fun decideForBoot(state: TakeoverState, licenseValid: Boolean, nowElapsedMs: Long): TakeoverDecision =
        decideForSystemLauncher(state, licenseValid, nowElapsedMs)

    private fun decideForSystemLauncher(state: TakeoverState, licenseValid: Boolean, nowElapsedMs: Long): TakeoverDecision {
        if (!state.takeoverEnabled) return TakeoverDecision.Ignore(IgnoreReason.TAKEOVER_DISABLED)
        if (isWindowActive(state.suppressUntilElapsedMs, nowElapsedMs, SUPPRESS_AFTER_DISABLE_MS)) {
            return TakeoverDecision.Ignore(IgnoreReason.SUPPRESSED_AFTER_DISABLE)
        }
        if (isWindowActive(state.pausedUntilElapsedMs, nowElapsedMs, LOOP_PAUSE_MS)) {
            return TakeoverDecision.Ignore(IgnoreReason.PAUSED)
        }
        if (!licenseValid) return TakeoverDecision.Ignore(IgnoreReason.LICENSE_INVALID)

        val opensInWindow = state.recentOpensElapsedMs.filter { nowElapsedMs - it in 0 until loopWindowMs }
        if (opensInWindow.any { nowElapsedMs - it < minIntervalBetweenOpensMs }) {
            return TakeoverDecision.Ignore(IgnoreReason.RECENTLY_OPENED)
        }
        if (opensInWindow.size >= maxOpensInWindow) return TakeoverDecision.PauseForLoop
        return TakeoverDecision.Open
    }

    /**
     * Uma janela está ativa se termina no futuro, mas não além da sua duração máxima: valores gravados
     * antes de a TV reiniciar (relógio monotônico zerado) ficam muito à frente e contam como expirados.
     */
    private fun isWindowActive(untilElapsedMs: Long, nowElapsedMs: Long, maxDurationMs: Long): Boolean =
        untilElapsedMs > nowElapsedMs && untilElapsedMs - nowElapsedMs <= maxDurationMs

    companion object {
        const val SUPPRESS_AFTER_DISABLE_MS = 5_000L
        const val LOOP_PAUSE_MS = 30_000L
        const val OPEN_FAILURE_RETRY_MS = 10_000L
    }
}
