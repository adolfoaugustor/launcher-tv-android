package br.tematv.launcher.takeover

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import br.tematv.launcher.TemaTvApplication
import br.tematv.launcher.home.HomeActivity
import java.lang.ref.WeakReference
import java.time.Instant

/**
 * Abre a tela temática quando o launcher do sistema aparece (RF-02, RF-04) e logo após o boot (RF-03, D-05).
 * Recebe só eventos de troca de janela dos launchers do sistema e não lê o conteúdo das telas.
 * Um serviço de acessibilidade ligado pode abrir atividades em segundo plano no Android 10+.
 */
class HomeTakeoverService : AccessibilityService() {

    private val policy = TakeoverPolicy()
    private val licenseGate: LicenseGate = AlwaysValidLicenseGate
    private val recentOpensElapsedMs = ArrayDeque<Long>()
    private lateinit var preferences: TakeoverPreferences
    private lateinit var themeCover: ThemeCover

    override fun onServiceConnected() {
        super.onServiceConnected()
        preferences = TakeoverPreferences(this)
        themeCover = ThemeCover(this)
        connectedService = WeakReference(this)
        Log.i(LOG_TAG, "Troca da tela inicial: serviço conectado")
        val nowElapsedMs = SystemClock.elapsedRealtime()
        if (nowElapsedMs < BOOT_GRACE_MS) {
            apply(policy.decideForBoot(currentState(nowElapsedMs), licenseGate.isLicenseValid(), nowElapsedMs), "boot")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val nowElapsedMs = SystemClock.elapsedRealtime()
        val launcherPackage = event.packageName?.toString()
        apply(policy.decide(launcherPackage, currentState(nowElapsedMs), licenseGate.isLicenseValid(), nowElapsedMs), launcherPackage)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (::themeCover.isInitialized) themeCover.hide()
        connectedService = null
        super.onDestroy()
    }

    private fun currentState(nowElapsedMs: Long): TakeoverState {
        while (recentOpensElapsedMs.isNotEmpty() && nowElapsedMs - recentOpensElapsedMs.first() >= RECENT_OPENS_WINDOW_MS) {
            recentOpensElapsedMs.removeFirst()
        }
        return preferences.snapshot(recentOpensElapsedMs.toList())
    }

    private fun apply(decision: TakeoverDecision, trigger: String?) {
        val nowElapsedMs = SystemClock.elapsedRealtime()
        when (decision) {
            TakeoverDecision.Open -> openThemedHome(nowElapsedMs, trigger)
            TakeoverDecision.PauseForLoop -> {
                preferences.pauseForLoop(nowElapsedMs, Instant.now())
                recentOpensElapsedMs.clear()
                Log.w(LOG_TAG, "Troca da tela inicial: pausa de 30 s por repetição (gatilho $trigger)")
            }
            is TakeoverDecision.Ignore -> Unit
        }
    }

    private fun openThemedHome(nowElapsedMs: Long, trigger: String?) {
        val intent = Intent(this, HomeActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP,
        )
        // A capa aparece na hora; a tela temática chega depois da espera que o Android impõe após o Home (RF-14).
        themeCover.show((application as TemaTvApplication).themeRepository.activeTheme.value)
        try {
            startActivity(intent)
            recentOpensElapsedMs.addLast(nowElapsedMs)
            preferences.recordTakeover(Instant.now())
            Log.i(LOG_TAG, "Troca da tela inicial: tela temática aberta (gatilho $trigger)")
        } catch (exception: RuntimeException) {
            themeCover.hide()
            preferences.pauseAfterOpenFailure(nowElapsedMs, Instant.now())
            Log.w(LOG_TAG, "Troca da tela inicial: falha ao abrir a tela temática: ${exception.message}")
        }
    }

    companion object {
        private const val LOG_TAG = "TemaTV"

        @Volatile
        private var connectedService: WeakReference<HomeTakeoverService>? = null

        /** Chamado pela tela temática quando ganha foco: a capa já não é necessária. */
        fun onThemedHomeVisible() {
            connectedService?.get()?.let { service -> service.themeCover.hide() }
        }

        private const val BOOT_GRACE_MS = 3 * 60 * 1000L
        private const val RECENT_OPENS_WINDOW_MS = 10_000L
    }
}
