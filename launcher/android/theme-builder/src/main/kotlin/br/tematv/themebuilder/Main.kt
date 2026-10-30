package br.tematv.themebuilder

import br.tematv.themebuilder.install.InstallLog
import br.tematv.themebuilder.install.InstallService
import br.tematv.themebuilder.install.ProcessCommandRunner
import java.io.File
import java.net.BindException
import kotlin.system.exitProcess

private const val FIRST_PORT = 8787
private const val LAST_PORT = 8797

fun main(args: Array<String>) {
    val home = File(System.getProperty("user.home"))
    val presets = Presets.load()
    val themesDir = File(home, "tematv/temas")
    val store = ProjectStore(File(home, "tematv/clientes"), presets)
    val activation = ResellerActivation(File(home, ".config/tematv/activation-secret"), File(home, "tematv/ativacoes.csv"))
    val server = LocalServer(
        store = store,
        images = ImageProcessor(),
        builder = PackageBuilder(themesDir, presets),
        activation = activation,
        presets = presets,
        themesDir = themesDir,
        install = InstallService(
            runner = ProcessCommandRunner(),
            launcherDir = File(home, "tematv/launcher"),
            log = InstallLog(File(home, "tematv/instalacoes")),
            activation = activation,
            store = store,
            themesDir = themesDir,
        ),
    )
    val port = (FIRST_PORT..LAST_PORT).firstOrNull { port ->
        try {
            server.start(port)
            true
        } catch (exception: BindException) {
            false
        }
    }
    if (port == null) {
        System.err.println("As portas $FIRST_PORT a $LAST_PORT estão ocupadas. Feche outra cópia do tematv e tente de novo.")
        exitProcess(1)
    }
    val url = "http://localhost:$port"
    println("TemaTV aberto em $url (Ctrl+C para fechar)")
    if ("--sem-navegador" !in args) openBrowser(url)
}

/** No WSL abre o navegador do Windows; fora dele, o navegador padrão do Linux (D-13). */
private fun openBrowser(url: String) {
    val isWsl = System.getenv("WSL_DISTRO_NAME") != null || File("/proc/sys/fs/binfmt_misc/WSLInterop").exists()
    val command = if (isWsl) listOf("cmd.exe", "/c", "start", "", url) else listOf("xdg-open", url)
    try {
        ProcessBuilder(command)
            .apply { if (isWsl) directory(File("/mnt/c").takeIf { it.isDirectory }) }
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
    } catch (exception: java.io.IOException) {
        println("Não consegui abrir o navegador sozinho: abra $url")
    }
}
