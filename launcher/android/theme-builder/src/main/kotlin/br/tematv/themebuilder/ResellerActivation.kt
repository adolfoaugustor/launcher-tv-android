package br.tematv.themebuilder

import br.tematv.activation.ActivationCode
import br.tematv.activation.ActivationDigits
import java.io.File
import java.time.Clock
import java.time.Instant

data class GeneratedActivation(val deviceCode: String, val activationCode: String)

/**
 * Senha de ativação pela página, com o mesmo cálculo e o mesmo registro do `activation-tool` (D-08, RF-12, RF-13).
 * O segredo é lido do arquivo a cada pedido e nunca sai desta classe (RN-04).
 */
class ResellerActivation(
    val secretFile: File,
    private val activationLogFile: File,
    private val clock: Clock = Clock.systemUTC(),
) {

    val isSecretAvailable: Boolean get() = secretFile.isFile

    fun generate(deviceCodeInput: String, customer: String): GeneratedActivation {
        val deviceCode = ActivationDigits.normalize(deviceCodeInput)
            ?: throw UserInputException("device_code", "Código do aparelho inválido: use os 12 números mostrados na TV.")
        if (!isSecretAvailable) {
            throw UserInputException(null, "Segredo de ativação não encontrado em $secretFile. Restaure o arquivo do backup.", 409)
        }
        val activationCode = ActivationCode.expectedFor(secretFile.readText(), deviceCode)
        activationLogFile.parentFile.mkdirs()
        activationLogFile.appendText(logLine(deviceCode, customer.trim()))
        return GeneratedActivation(ActivationDigits.format(deviceCode), ActivationDigits.format(activationCode))
    }

    /** Mesmo formato do `activation-tool senha`: `instante,código,cliente`. */
    private fun logLine(deviceCode: String, customer: String): String =
        "${Instant.now(clock)},${ActivationDigits.format(deviceCode)},${csvField(customer)}\n"

    private fun csvField(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' }) "\"" + value.replace("\"", "\"\"") + "\"" else value
}
