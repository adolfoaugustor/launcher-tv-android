package br.tematv.themebuilder.install

import br.tematv.themebuilder.ProjectStore
import br.tematv.themebuilder.ResellerActivation
import br.tematv.themebuilder.UserInputException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

@Serializable
data class InstallRequest(
    @SerialName("install_id") val installId: String,
    val ip: String,
    @SerialName("theme_id") val themeId: String,
    @SerialName("device_code") val deviceCode: String? = null,
)

@Serializable
data class StepResponse(
    val status: String,
    val message: String,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("device_code") val deviceCode: String? = null,
)

@Serializable
data class InstallEnvironment(
    @SerialName("adb_available") val adbAvailable: Boolean,
    @SerialName("adb_install_command") val adbInstallCommand: String,
    val launcher: LauncherReleaseInfo?,
    @SerialName("launcher_missing_hint") val launcherMissingHint: String,
)

/** Aba "Instalar na TV" (`interfaces/api-instalacao.md`): valida o pedido, roda a etapa e registra. */
class InstallService(
    private val runner: CommandRunner,
    private val launcherDir: File,
    private val log: InstallLog,
    private val activation: ResellerActivation,
    private val store: ProjectStore,
    private val themesDir: File,
    private val portProbe: (String) -> Boolean = ::isAdbPortOpen,
) {

    fun environment(): InstallEnvironment {
        val adbAvailable = try {
            runner.run(listOf("adb", "version"), null, null, 10).succeeded
        } catch (exception: IOException) {
            false
        }
        return InstallEnvironment(
            adbAvailable = adbAvailable,
            adbInstallCommand = "sudo apt install -y adb",
            launcher = LauncherRelease.find(launcherDir)?.info,
            launcherMissingHint = "No notebook, crie a chave (tools/gerar-chave-assinatura.sh) e rode tools/instalar-tematv.sh.",
        )
    }

    fun check(ip: String): StepOutcome = steps(requireValidIp(ip)).findTv(ip)

    fun runStep(step: String, request: InstallRequest): StepOutcome {
        val ip = requireValidIp(request.ip)
        val project = store.get(request.themeId)
        val packageFile = File(themesDir, "${project.themeId}.tematv").takeIf { it.isFile }
            ?: throw UserInputException("theme_id", "Gere o pacote deste cliente antes de instalar.")
        val steps = steps(ip)
        val startedAt = System.currentTimeMillis()
        val outcome = when (step) {
            "conectar" -> steps.connect()
            "instalar" -> steps.installLauncher(LauncherRelease.find(launcherDir))
            "inicio-automatico" -> steps.allowAutoStart()
            "codigo" -> steps.readDeviceCode()
            "tema" -> steps.sendTheme(packageFile, project.themeId)
            "ativacao" -> steps.activate(request.deviceCode, project.displayName)
            "acessibilidade" -> steps.enableTakeover()
            "conferencia" -> steps.verifyHome()
            else -> throw UserInputException(null, "Etapa desconhecida: $step", 404)
        }
        record(step, request, outcome, System.currentTimeMillis() - startedAt)
        return outcome
    }

    fun undo(request: InstallRequest): StepOutcome {
        val ip = requireValidIp(request.ip)
        val startedAt = System.currentTimeMillis()
        val outcome = steps(ip).undo()
        record("desfazer", request, outcome, System.currentTimeMillis() - startedAt)
        return outcome
    }

    private fun steps(ip: String) = InstallSteps(Adb(runner, ip), activation, portProbe)

    private fun record(step: String, request: InstallRequest, outcome: StepOutcome, elapsedMs: Long) {
        log.append(
            InstallLogEntry(
                timestamp = "",
                installId = request.installId,
                clientThemeId = request.themeId,
                tvIp = request.ip,
                tvManufacturer = outcome.tvInfo?.manufacturer,
                tvModel = outcome.tvInfo?.model,
                androidVersion = outcome.tvInfo?.androidVersion,
                deviceCode = outcome.deviceCode ?: request.deviceCode,
                step = step,
                result = outcome.status.name.lowercase(),
                detail = outcome.message,
                elapsedMs = elapsedMs,
            ),
        )
    }

    companion object {
        private val ipv4Pattern = Regex("^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$")

        fun requireValidIp(ip: String): String =
            ip.trim().takeIf { ipv4Pattern.matches(it) } ?: throw UserInputException("ip", "IP inválido: use 4 números de 0 a 255, como 192.168.1.2.")

        private fun isAdbPortOpen(ip: String): Boolean = try {
            Socket().use { it.connect(InetSocketAddress(ip, Adb.PORT), 5_000) }
            true
        } catch (exception: IOException) {
            false
        }
    }
}

fun StepOutcome.toResponse() = StepResponse(status.name.lowercase(), message, errorCode, deviceCode)
