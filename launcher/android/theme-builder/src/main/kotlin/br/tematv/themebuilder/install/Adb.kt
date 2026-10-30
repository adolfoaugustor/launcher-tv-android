package br.tematv.themebuilder.install

import java.io.File
import java.util.concurrent.TimeUnit

data class CommandResult(val exitCode: Int, val output: String) {
    val succeeded: Boolean get() = exitCode == 0
}

/** Executa um comando do notebook; `stdin` ou `stdinFile` alimentam a entrada padrão. */
fun interface CommandRunner {
    fun run(command: List<String>, stdin: ByteArray?, stdinFile: File?, timeoutSeconds: Long): CommandResult
}

class ProcessCommandRunner : CommandRunner {
    override fun run(command: List<String>, stdin: ByteArray?, stdinFile: File?, timeoutSeconds: Long): CommandResult {
        val outputFile = File.createTempFile("tematv-comando-", ".txt")
        try {
            val builder = ProcessBuilder(command).redirectErrorStream(true).redirectOutput(outputFile)
            if (stdinFile != null) builder.redirectInput(stdinFile)
            val process = builder.start()
            if (stdinFile == null) process.outputStream.use { stream -> stdin?.let(stream::write) }
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                return CommandResult(TIMEOUT_EXIT_CODE, outputFile.readText() + "\n(tempo esgotado após $timeoutSeconds s)")
            }
            return CommandResult(process.exitValue(), outputFile.readText())
        } finally {
            outputFile.delete()
        }
    }

    companion object {
        const val TIMEOUT_EXIT_CODE = -1
    }
}

/** Comandos do ADB sempre para a TV escolhida (`-s <ip>:5555`, RN-04). */
class Adb(private val runner: CommandRunner, ip: String, private val adbCommand: String = "adb") {

    val serial = "$ip:$PORT"

    fun connect(): CommandResult = run(listOf("connect", serial))

    fun disconnect(): CommandResult = run(listOf("disconnect", serial))

    /** `device`, `unauthorized`, `offline` ou a mensagem de erro do ADB. */
    fun state(): String = run(listOf("-s", serial, "get-state")).output.trim()

    fun shell(command: String, timeoutSeconds: Long = DEFAULT_TIMEOUT_SECONDS): CommandResult =
        run(listOf("-s", serial, "shell", command), timeoutSeconds = timeoutSeconds)

    fun install(apk: File): CommandResult = run(listOf("-s", serial, "install", "-r", apk.path), timeoutSeconds = INSTALL_TIMEOUT_SECONDS)

    fun uninstall(): CommandResult = run(listOf("-s", serial, "uninstall", LAUNCHER_PACKAGE), timeoutSeconds = INSTALL_TIMEOUT_SECONDS)

    fun contentRead(name: String): CommandResult = shell("content read --uri $IMPORT_URI/$name")

    fun contentWrite(name: String, bytes: ByteArray): CommandResult =
        run(listOf("-s", serial, "shell", "content write --uri $IMPORT_URI/$name"), stdin = bytes)

    fun contentWrite(name: String, file: File): CommandResult =
        run(listOf("-s", serial, "shell", "content write --uri $IMPORT_URI/$name"), stdinFile = file, timeoutSeconds = INSTALL_TIMEOUT_SECONDS)

    /** Inicia o app sem abrir telas: a recarga publica o código, importa tema e senha e grava o estado. */
    fun reload(): CommandResult =
        shell("am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver")

    fun property(name: String): String = shell("getprop $name").output.trim()

    private fun run(
        arguments: List<String>,
        stdin: ByteArray? = null,
        stdinFile: File? = null,
        timeoutSeconds: Long = DEFAULT_TIMEOUT_SECONDS,
    ): CommandResult = runner.run(listOf(adbCommand) + arguments, stdin, stdinFile, timeoutSeconds)

    companion object {
        const val PORT = 5555
        const val LAUNCHER_PACKAGE = "br.tematv.launcher"
        private const val IMPORT_URI = "content://br.tematv.launcher.themes"
        private const val DEFAULT_TIMEOUT_SECONDS = 60L
        private const val INSTALL_TIMEOUT_SECONDS = 180L
    }
}
