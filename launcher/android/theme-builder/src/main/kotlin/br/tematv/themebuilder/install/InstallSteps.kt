package br.tematv.themebuilder.install

import br.tematv.activation.ActivationDigits
import br.tematv.themebuilder.ProjectStore
import br.tematv.themebuilder.ResellerActivation
import br.tematv.themebuilder.UserInputException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import java.io.File

enum class StepStatus { OK, SKIPPED, ERROR }

data class TvInfo(val manufacturer: String, val model: String, val androidVersion: String)

data class StepOutcome(
    val status: StepStatus,
    val message: String,
    val errorCode: String? = null,
    val deviceCode: String? = null,
    val tvInfo: TvInfo? = null,
)

/** `app_status.json` publicado pelo app (`interfaces/estado-do-app.md`). */
@Serializable
data class TvAppStatus(
    @SerialName("version_code") val versionCode: Long,
    @SerialName("signer_sha256") val signerSha256: String,
    val activated: Boolean,
    @SerialName("active_theme_id") val activeThemeId: String? = null,
)

/**
 * Etapas da instalação numa TV (RF-03 a RF-11, RF-14). Cada uma pode ser repetida e devolve ok, pulada ou erro
 * com a mensagem para a lista de verificação; nenhuma lança erro por falha da TV.
 */
class InstallSteps(
    private val adb: Adb,
    private val activation: ResellerActivation,
    private val portProbe: (String) -> Boolean,
    private val sleep: (Long) -> Unit = Thread::sleep,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {

    fun findTv(ip: String): StepOutcome =
        if (portProbe(ip)) {
            StepOutcome(StepStatus.OK, "TV encontrada em $ip.")
        } else {
            StepOutcome(
                StepStatus.ERROR,
                "TV desligada, fora da rede ou sem depuração por rede em $ip. Ligue a TV com o controle, confira o IP " +
                    "em Configurações › Rede e se a depuração por rede está ligada nas Opções do desenvolvedor.",
                errorCode = "tv-nao-encontrada",
            )
        }

    fun connect(): StepOutcome {
        adb.connect()
        var lastState = ""
        var reconnected = false
        val authorized = waitUntil(AUTHORIZATION_TIMEOUT_MS, POLL_INTERVAL_MS) {
            lastState = adb.state()
            when {
                lastState == "device" -> true
                lastState == "offline" && !reconnected -> {
                    adb.disconnect()
                    adb.connect()
                    reconnected = true
                    false
                }
                lastState != "unauthorized" && lastState != "offline" -> {
                    adb.connect()
                    false
                }
                else -> false
            }
        }
        if (!authorized) {
            val message = if (lastState.contains("unauthorized")) {
                "A TV não autorizou este computador. Aceite o aviso \"Permitir depuração?\" na tela da TV, marque \"Sempre permitir\" e tente de novo."
            } else {
                "A TV não respondeu ao ADB ($lastState). Confira se ela está ligada e tente de novo."
            }
            return StepOutcome(StepStatus.ERROR, message, errorCode = "sem-autorizacao")
        }
        val tvInfo = TvInfo(
            manufacturer = adb.property("ro.product.manufacturer"),
            model = adb.property("ro.product.model"),
            androidVersion = adb.property("ro.build.version.release"),
        )
        return StepOutcome(StepStatus.OK, "Conectado a ${tvInfo.manufacturer} ${tvInfo.model} (Android ${tvInfo.androidVersion}).", tvInfo = tvInfo)
    }

    fun installLauncher(release: LauncherRelease?): StepOutcome {
        if (release == null) {
            return StepOutcome(
                StepStatus.ERROR,
                "APK de produção não encontrado. No notebook, rode tools/instalar-tematv.sh (com a chave de assinatura criada).",
                errorCode = "apk-ausente",
            )
        }
        adb.reload()
        val status = readStatus(STATUS_PROBE_TIMEOUT_MS)
        if (status != null && status.versionCode == release.info.versionCode &&
            release.info.signerSha256 != null && status.signerSha256.equals(release.info.signerSha256, ignoreCase = true)
        ) {
            return StepOutcome(StepStatus.SKIPPED, "Já instalado (versão ${release.info.versionName}).")
        }
        val result = adb.install(release.apk)
        val failure = Regex("INSTALL_FAILED_[A-Z_]+").find(result.output)?.value
        return when {
            failure == "INSTALL_FAILED_UPDATE_INCOMPATIBLE" -> StepOutcome(
                StepStatus.ERROR,
                "A TV tem o launcher assinado com outra chave. Use \"Desfazer e instalar de novo\": a TV perde a ativação atual e é ativada de novo.",
                errorCode = "assinatura-diferente",
            )
            failure == "INSTALL_FAILED_INSUFFICIENT_STORAGE" -> StepOutcome(
                StepStatus.ERROR,
                "Pouco espaço na TV ($failure). Libere espaço desinstalando apps sem uso e tente de novo.",
                errorCode = failure,
            )
            failure != null -> StepOutcome(StepStatus.ERROR, "A TV recusou a instalação ($failure).", errorCode = failure)
            !result.succeeded || !result.output.contains("Success") ->
                StepOutcome(StepStatus.ERROR, "Falha ao instalar: ${result.output.trim().takeLast(300)}", errorCode = "instalacao")
            else -> StepOutcome(StepStatus.OK, "Launcher ${release.info.versionName} instalado.")
        }
    }

    fun allowAutoStart(): StepOutcome {
        val set = adb.shell("appops set ${Adb.LAUNCHER_PACKAGE} APP_AUTO_START allow")
        if (set.output.contains("Unknown operation", ignoreCase = true)) {
            return StepOutcome(StepStatus.SKIPPED, "Não se aplica a esta TV.")
        }
        val get = adb.shell("appops get ${Adb.LAUNCHER_PACKAGE} APP_AUTO_START")
        return if (set.succeeded && get.output.contains("allow")) {
            StepOutcome(StepStatus.OK, "Início automático liberado.")
        } else {
            StepOutcome(StepStatus.ERROR, "Não foi possível liberar o início automático: ${(set.output + get.output).trim().takeLast(300)}", errorCode = "inicio-automatico")
        }
    }

    fun readDeviceCode(): StepOutcome {
        adb.reload()
        var deviceCode: String? = null
        waitUntil(IMPORT_TIMEOUT_MS, POLL_INTERVAL_MS) {
            val result = adb.contentRead(DEVICE_CODE_FILE_NAME)
            deviceCode = result.takeIf { it.succeeded }?.output?.trim()?.let(ActivationDigits::normalize)
            if (deviceCode == null) adb.reload()
            deviceCode != null
        }
        val code = deviceCode
            ?: return StepOutcome(StepStatus.ERROR, "A TV não informou o código do aparelho em 30 s. Tente de novo.", errorCode = "codigo")
        val formatted = ActivationDigits.format(code)
        return StepOutcome(StepStatus.OK, "Código do aparelho: $formatted.", deviceCode = formatted)
    }

    fun sendTheme(packageFile: File, themeId: String): StepOutcome {
        val packageName = "$themeId.tematv"
        if (!writeWithRetry { adb.contentWrite(packageName, packageFile) }) {
            return StepOutcome(StepStatus.ERROR, "A TV não aceitou o pacote do tema pela importação. Tente de novo.", errorCode = "tema")
        }
        if (!writeWithRetry { adb.contentWrite(ACTIVE_THEME_FILE_NAME, "$packageName\n".toByteArray()) }) {
            return StepOutcome(StepStatus.ERROR, "A TV não aceitou a escolha do tema ativo. Tente de novo.", errorCode = "tema")
        }
        adb.reload()
        var status: TvAppStatus? = null
        waitUntil(APPLY_TIMEOUT_MS, POLL_INTERVAL_MS) {
            status = readStatusOnce()
            status?.activeThemeId == themeId
        }
        return if (status?.activeThemeId == themeId) {
            StepOutcome(StepStatus.OK, "Tema $themeId aplicado.")
        } else {
            StepOutcome(
                StepStatus.ERROR,
                "A TV não aplicou o tema $themeId (tema atual: ${status?.activeThemeId ?: "padrão"}). Veja o último erro em \"Tema\" na TV.",
                errorCode = "tema",
            )
        }
    }

    fun activate(deviceCode: String?, customer: String): StepOutcome {
        val code = deviceCode?.let(ActivationDigits::normalize)
            ?: return StepOutcome(StepStatus.ERROR, "Código do aparelho ausente: refaça a etapa do código.", errorCode = "codigo")
        if (readStatus(STATUS_PROBE_TIMEOUT_MS)?.activated == true) {
            return StepOutcome(StepStatus.SKIPPED, "A TV já está ativada.")
        }
        val activationCode = try {
            activation.generate(code, customer).activationCode
        } catch (exception: UserInputException) {
            return StepOutcome(StepStatus.ERROR, exception.message.orEmpty(), errorCode = "segredo")
        }
        if (!writeWithRetry { adb.contentWrite(ACTIVATION_CODE_FILE_NAME, "$activationCode\n".toByteArray()) }) {
            return StepOutcome(StepStatus.ERROR, "A TV não aceitou a senha pela importação. Tente de novo.", errorCode = "ativacao")
        }
        adb.reload()
        val activated = waitUntil(APPLY_TIMEOUT_MS, POLL_INTERVAL_MS) { readStatusOnce()?.activated == true }
        return if (activated) {
            StepOutcome(StepStatus.OK, "TV ativada.")
        } else {
            StepOutcome(StepStatus.ERROR, "A TV não aceitou a senha. Confira se o segredo de ativação é o mesmo usado para compilar o APK.", errorCode = "ativacao")
        }
    }

    fun enableTakeover(): StepOutcome {
        val current = adb.shell("settings get secure $ACCESSIBILITY_SETTING").output
        val updated = AccessibilityList.withService(current)
        if (AccessibilityList.parse(current).joinToString(":") != updated) {
            val put = adb.shell("settings put secure $ACCESSIBILITY_SETTING ${AccessibilityList.shellQuote(updated)}")
            if (!put.succeeded) return StepOutcome(StepStatus.ERROR, "Não foi possível ligar o serviço: ${put.output.trim()}", errorCode = "acessibilidade")
        }
        adb.shell("settings put secure accessibility_enabled 1")
        val enabled = waitUntil(ACCESSIBILITY_TIMEOUT_MS, POLL_INTERVAL_MS) { isServiceEnabled() }
        return if (enabled) {
            StepOutcome(StepStatus.OK, "Troca da tela inicial ligada.")
        } else {
            StepOutcome(StepStatus.ERROR, "O Android não ligou o serviço da troca da tela inicial em 15 s. Tente de novo.", errorCode = "acessibilidade")
        }
    }

    fun verifyHome(): StepOutcome {
        var focused: String? = null
        repeat(HOME_ATTEMPTS) {
            adb.shell("input keyevent KEYCODE_HOME")
            val found = waitUntil(HOME_TIMEOUT_MS, POLL_INTERVAL_MS) {
                focused = focusedPackage()
                focused == Adb.LAUNCHER_PACKAGE
            }
            if (found) return StepOutcome(StepStatus.OK, "Tela temática na frente depois do Home.")
        }
        return StepOutcome(
            StepStatus.ERROR,
            "Depois do Home a TV mostrou ${focused ?: "nenhuma janela"}, não a tela temática. Confira o aviso em \"Tema\" na TV.",
            errorCode = "conferencia",
        )
    }

    fun undo(): StepOutcome {
        val current = adb.shell("settings get secure $ACCESSIBILITY_SETTING").output
        if (AccessibilityList.contains(current)) {
            val remaining = AccessibilityList.withoutService(current)
            adb.shell("settings put secure $ACCESSIBILITY_SETTING ${AccessibilityList.shellQuote(remaining)}")
            if (remaining.isEmpty()) adb.shell("settings put secure accessibility_enabled 0")
        }
        val result = adb.uninstall()
        return when {
            result.output.contains("Success") -> StepOutcome(StepStatus.OK, "Launcher removido; o Home volta ao launcher do Google.")
            result.output.contains("Unknown package") || result.output.contains("DELETE_FAILED_INTERNAL_ERROR") ->
                StepOutcome(StepStatus.SKIPPED, "O launcher não estava instalado.")
            else -> StepOutcome(StepStatus.ERROR, "Falha ao remover o launcher: ${result.output.trim().takeLast(300)}", errorCode = "desfazer")
        }
    }

    private fun isServiceEnabled(): Boolean {
        val dump = adb.shell("dumpsys accessibility").output
        val enabledLine = dump.lines().firstOrNull { it.contains("Enabled services", ignoreCase = true) } ?: return false
        return enabledLine.contains(AccessibilityList.SERVICE) || enabledLine.contains(SHORT_SERVICE_NAME)
    }

    /** Pacote da linha `mCurrentFocus` não nula (a TCL lista várias; lição da feature 003). */
    private fun focusedPackage(): String? =
        adb.shell("dumpsys window | grep mCurrentFocus").output.lines()
            .filterNot { it.contains("=null") }
            .firstNotNullOfOrNull { focusPattern.find(it)?.groupValues?.get(1) }

    private fun readStatus(timeoutMs: Long): TvAppStatus? {
        var status: TvAppStatus? = null
        waitUntil(timeoutMs, POLL_INTERVAL_MS) {
            status = readStatusOnce()
            status != null
        }
        return status
    }

    private fun readStatusOnce(): TvAppStatus? {
        val result = adb.contentRead(APP_STATUS_FILE_NAME)
        if (!result.succeeded) return null
        val json = result.output.lines().firstOrNull { it.trimStart().startsWith("{") } ?: return null
        return try {
            ProjectStore.json.decodeFromString(TvAppStatus.serializer(), json)
        } catch (exception: SerializationException) {
            null
        }
    }

    /** A importação pode não responder por alguns segundos logo depois de instalar (lição das features 002 e 004). */
    private fun writeWithRetry(write: () -> CommandResult): Boolean =
        waitUntil(IMPORT_TIMEOUT_MS, POLL_INTERVAL_MS) { write().succeeded }

    private fun waitUntil(timeoutMs: Long, intervalMs: Long, condition: () -> Boolean): Boolean {
        val deadline = nowMs() + timeoutMs
        while (true) {
            if (condition()) return true
            if (nowMs() >= deadline) return false
            sleep(intervalMs)
        }
    }

    private companion object {
        const val DEVICE_CODE_FILE_NAME = "device_code.txt"
        const val ACTIVATION_CODE_FILE_NAME = "activation_code.txt"
        const val ACTIVE_THEME_FILE_NAME = "active_theme.txt"
        const val APP_STATUS_FILE_NAME = "app_status.json"
        const val ACCESSIBILITY_SETTING = "enabled_accessibility_services"
        const val SHORT_SERVICE_NAME = "br.tematv.launcher/.takeover.HomeTakeoverService"
        const val POLL_INTERVAL_MS = 1_000L
        const val AUTHORIZATION_TIMEOUT_MS = 120_000L
        const val IMPORT_TIMEOUT_MS = 30_000L
        const val APPLY_TIMEOUT_MS = 20_000L
        const val STATUS_PROBE_TIMEOUT_MS = 5_000L
        const val ACCESSIBILITY_TIMEOUT_MS = 15_000L
        const val HOME_TIMEOUT_MS = 5_000L
        const val HOME_ATTEMPTS = 3
        val focusPattern = Regex("mCurrentFocus=Window\\{\\S+ u\\d+ ([A-Za-z0-9_.]+)")
    }
}
