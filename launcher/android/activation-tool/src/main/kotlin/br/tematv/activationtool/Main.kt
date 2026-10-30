package br.tematv.activationtool

import br.tematv.activation.ActivationCode
import br.tematv.activation.ActivationDigits
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.time.Instant
import kotlin.system.exitProcess

private val HOME = File(System.getProperty("user.home"))
private val SECRET_FILE = File(HOME, ".config/tematv/activation-secret")
private val ACTIVATION_LOG_FILE = File(HOME, "tematv/ativacoes.csv")

private const val USAGE = """Uso:
  activation-tool gerar-segredo
      Gera o segredo de ativação do revendedor (uma única vez).
  activation-tool senha <código do aparelho> [--cliente <nome>]
      Mostra a senha de ativação da TV e registra em ~/tematv/ativacoes.csv."""

fun main(args: Array<String>) {
    exitProcess(
        when (args.firstOrNull()) {
            "gerar-segredo" -> generateSecret()
            "senha" -> printActivationCode(args.drop(1))
            else -> fail(USAGE)
        },
    )
}

private fun generateSecret(): Int {
    if (SECRET_FILE.exists()) {
        return fail("Já existe um segredo em $SECRET_FILE. Use o existente: gerar outro invalidaria as senhas já entregues.")
    }
    SECRET_FILE.parentFile.mkdirs()
    SECRET_FILE.writeText(ActivationCode.generateSecretHex() + "\n")
    Files.setPosixFilePermissions(SECRET_FILE.toPath(), PosixFilePermissions.fromString("rw-------"))
    println("Segredo de ativação criado em $SECRET_FILE (permissão 600).")
    println("Faça uma cópia de segurança deste arquivo: sem ele não é possível ativar novas TVs com o mesmo APK.")
    return 0
}

private fun printActivationCode(arguments: List<String>): Int {
    val deviceCodeArgument = arguments.firstOrNull() ?: return fail(USAGE)
    val customer = arguments.indexOf("--cliente").takeIf { it >= 0 }?.let { arguments.getOrNull(it + 1) }
        ?: if ("--cliente" in arguments) return fail("Informe o nome depois de --cliente.") else ""
    val deviceCode = ActivationDigits.normalize(deviceCodeArgument)
        ?: return fail("Código do aparelho inválido: \"$deviceCodeArgument\". Use os 12 números mostrados na TV.")
    if (!SECRET_FILE.isFile) {
        return fail("Segredo de ativação não encontrado em $SECRET_FILE. Gere com: activation-tool gerar-segredo")
    }
    val activationCode = ActivationCode.expectedFor(SECRET_FILE.readText(), deviceCode)

    ACTIVATION_LOG_FILE.parentFile.mkdirs()
    ACTIVATION_LOG_FILE.appendText("${Instant.now()},${ActivationDigits.format(deviceCode)},${csvField(customer)}\n")

    println("Código do aparelho: ${ActivationDigits.format(deviceCode)}")
    println("Senha de ativação:")
    println(ActivationDigits.format(activationCode))
    return 0
}

private fun csvField(value: String): String =
    if (value.any { it == ',' || it == '"' || it == '\n' }) "\"" + value.replace("\"", "\"\"") + "\"" else value

private fun fail(message: String): Int {
    System.err.println(message)
    return 1
}
