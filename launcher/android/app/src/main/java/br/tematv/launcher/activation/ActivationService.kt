package br.tematv.launcher.activation

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import br.tematv.activation.ActivationAttempts
import br.tematv.activation.ActivationCode
import br.tematv.activation.ActivationDigits
import br.tematv.activation.DeviceCode
import br.tematv.launcher.BuildConfig
import br.tematv.launcher.takeover.LicenseGate
import br.tematv.launcher.theme.ThemeImportNames
import br.tematv.launcher.theme.ThemeStorage
import java.io.IOException
import java.time.Instant

sealed interface ActivationResult {
    data object Activated : ActivationResult
    data object WrongCode : ActivationResult
    data object InvalidFormat : ActivationResult
    data class Locked(val remainingMs: Long) : ActivationResult
}

/** Ativação da TV por senha de 12 números (feature 004): conferência, tentativas e troca de arquivos pelo ADB. */
class ActivationService(
    private val context: Context,
    private val store: ActivationStore = ActivationStore(context),
    private val secretHex: String = BuildConfig.ACTIVATION_SECRET,
) {

    /** Código do aparelho, estável para o app nesta TV até a restauração de fábrica (D-02). */
    val deviceCode: String by lazy {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
        DeviceCode.fromAndroidId(androidId, context.packageName)
    }

    val formattedDeviceCode: String get() = ActivationDigits.format(deviceCode)
    val activatedAt: String? get() = store.activatedAt

    /** Reconfere a senha guardada contra o código atual: restauração de fábrica muda o código e invalida (RF-07). */
    fun isActivated(): Boolean = store.activationCode?.let { ActivationCode.matches(secretHex, deviceCode, it) } == true

    fun tryActivate(typedCode: String, nowElapsedMs: Long = SystemClock.elapsedRealtime()): ActivationResult {
        val remainingLockMs = ActivationAttempts.remainingLockMs(store.attempts, nowElapsedMs)
        if (remainingLockMs > 0) return ActivationResult.Locked(remainingLockMs)
        val normalized = ActivationDigits.normalize(typedCode) ?: return ActivationResult.InvalidFormat

        if (ActivationCode.matches(secretHex, deviceCode, normalized)) {
            store.saveActivation(normalized, Instant.now())
            Log.i(LOG_TAG, "Ativação: aceita")
            return ActivationResult.Activated
        }
        val attempts = ActivationAttempts.afterFailure(store.attempts, nowElapsedMs)
        store.saveAttempts(attempts)
        Log.w(LOG_TAG, "Ativação: senha recusada")
        val lockMs = ActivationAttempts.remainingLockMs(attempts, nowElapsedMs)
        return if (lockMs > 0) ActivationResult.Locked(lockMs) else ActivationResult.WrongCode
    }

    /** Grava `device_code.txt` na pasta de importação para o ADB ler (RF-02). */
    fun publishDeviceCode(storage: ThemeStorage) {
        try {
            if (!storage.ensureThemesDir()) return
            storage.packageFile(ThemeImportNames.DEVICE_CODE_FILE_NAME)?.writeText(formattedDeviceCode + "\n")
        } catch (exception: IOException) {
            Log.w(LOG_TAG, "Ativação: não foi possível gravar o código do aparelho: ${exception.message}")
        }
    }

    /** Importa a senha enviada pelo ADB em `activation_code.txt` e apaga o arquivo (RF-10). */
    fun importActivationCode(storage: ThemeStorage) {
        val file = try {
            storage.packageFile(ThemeImportNames.ACTIVATION_CODE_FILE_NAME)?.takeIf { it.isFile } ?: return
        } catch (exception: IOException) {
            return
        }
        val typedCode = file.readText()
        file.delete()
        tryActivate(typedCode)
    }

    private companion object {
        const val LOG_TAG = "TemaTV"
    }
}

class ActivationLicenseGate(private val activationService: ActivationService) : LicenseGate {
    override fun isLicenseValid(): Boolean = activationService.isActivated()
}
