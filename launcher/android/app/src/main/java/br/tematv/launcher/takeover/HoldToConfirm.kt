package br.tematv.launcher.takeover

/** Confirmação por segurar um botão (RF-13): só conclui depois de [requiredHoldMs] sem soltar. */
class HoldToConfirm(private val requiredHoldMs: Long = 3_000) {

    private var pressedAtMs: Long? = null

    val isHolding: Boolean get() = pressedAtMs != null

    /** Repetições da tecla enquanto ela continua apertada não recomeçam a contagem. */
    fun press(nowMs: Long) {
        if (pressedAtMs == null) pressedAtMs = nowMs
    }

    fun release() {
        pressedAtMs = null
    }

    fun progress(nowMs: Long): Float {
        val pressedAt = pressedAtMs ?: return 0f
        return ((nowMs - pressedAt).toFloat() / requiredHoldMs).coerceIn(0f, 1f)
    }

    fun isConfirmed(nowMs: Long): Boolean = progress(nowMs) >= 1f
}
