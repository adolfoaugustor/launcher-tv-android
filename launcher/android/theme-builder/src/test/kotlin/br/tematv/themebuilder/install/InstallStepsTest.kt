package br.tematv.themebuilder.install

import br.tematv.themebuilder.ResellerActivation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** ADB falso: cada regra responde aos comandos que contêm o trecho, em sequência (a última se repete). */
class FakeAdbRunner : CommandRunner {
    val commands = mutableListOf<String>()
    val stdinWrites = mutableListOf<Pair<String, String>>()
    private val rules = mutableListOf<Pair<String, ArrayDeque<CommandResult>>>()

    fun on(fragment: String, vararg results: CommandResult) {
        rules.add(0, fragment to ArrayDeque(results.toList()))
    }

    override fun run(command: List<String>, stdin: ByteArray?, stdinFile: File?, timeoutSeconds: Long): CommandResult {
        val text = command.joinToString(" ")
        commands += text
        stdin?.let { stdinWrites += text to it.decodeToString() }
        val queue = rules.firstOrNull { (fragment, _) -> text.contains(fragment) }?.second ?: return ok("")
        return if (queue.size > 1) queue.removeFirst() else queue.first()
    }
}

fun ok(output: String) = CommandResult(0, output)
fun fail(output: String) = CommandResult(1, output)

class InstallStepsTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val runner = FakeAdbRunner()
    private var now = 0L
    private val secret by lazy { File(folder.root, "secret").apply { writeText("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f") } }
    private val activationLog by lazy { File(folder.root, "ativacoes.csv") }
    private val steps by lazy {
        InstallSteps(
            adb = Adb(runner, "192.168.1.2"),
            activation = ResellerActivation(secret, activationLog),
            portProbe = { it == "192.168.1.2" },
            sleep = { now += it },
            nowMs = { now },
        )
    }
    private val release by lazy {
        LauncherRelease(folder.newFile("tematv-launcher.apk"), LauncherReleaseInfo(6, "0.6.0", "abc123"))
    }

    private fun status(versionCode: Int = 6, signer: String = "abc123", activated: Boolean = false, theme: String? = null) =
        ok("{\"version_code\":$versionCode,\"version_name\":\"x\",\"signer_sha256\":\"$signer\",\"activated\":$activated," +
            "\"active_theme_id\":${theme?.let { "\"$it\"" } ?: "null"},\"updated_at\":\"2026-10-30T00:00:00Z\"}\n")

    @Test
    fun `todos os comandos vão para a TV escolhida`() {
        runner.on("get-state", ok("device\n"))
        steps.connect()
        steps.enableTakeover()
        assertTrue(runner.commands.filterNot { it.startsWith("adb connect") || it.startsWith("adb disconnect") }
            .all { it.startsWith("adb -s 192.168.1.2:5555 ") })
    }

    @Test
    fun `TV encontrada pela porta 5555`() {
        assertEquals(StepStatus.OK, steps.findTv("192.168.1.2").status)
        assertEquals("tv-nao-encontrada", steps.findTv("192.168.1.9").errorCode)
    }

    @Test
    fun `espera a autorização e lê os dados da TV`() {
        runner.on("get-state", ok("unauthorized\n"), ok("unauthorized\n"), ok("device\n"))
        runner.on("ro.product.manufacturer", ok("TCL\n"))
        runner.on("ro.product.model", ok("Smart TV\n"))
        runner.on("ro.build.version.release", ok("11\n"))

        val outcome = steps.connect()

        assertEquals(StepStatus.OK, outcome.status)
        assertEquals(TvInfo("TCL", "Smart TV", "11"), outcome.tvInfo)
    }

    @Test
    fun `sem autorização em 120 s dá erro e offline reconecta uma vez`() {
        runner.on("get-state", ok("offline\n"), ok("unauthorized\n"))

        val outcome = steps.connect()

        assertEquals("sem-autorizacao", outcome.errorCode)
        assertTrue(outcome.message.contains("Permitir depuração"))
        assertEquals(1, runner.commands.count { it.startsWith("adb disconnect") })
        assertTrue(now >= 120_000)
    }

    @Test
    fun `pula a instalação com versão e assinatura iguais`() {
        runner.on("app_status.json", status())

        val outcome = steps.installLauncher(release)

        assertEquals(StepStatus.SKIPPED, outcome.status)
        assertFalse(runner.commands.any { " install " in it })
    }

    @Test
    fun `instala quando a assinatura é diferente ou o estado não existe`() {
        runner.on("app_status.json", status(signer = "outra"))
        runner.on(" install -r", ok("Performing Streamed Install\nSuccess\n"))
        assertEquals(StepStatus.OK, steps.installLauncher(release).status)

        runner.on("app_status.json", fail("Error: nome não aceito"))
        assertEquals(StepStatus.OK, steps.installLauncher(release).status)
    }

    @Test
    fun `assinatura incompatível vira erro próprio`() {
        runner.on("app_status.json", fail(""))
        runner.on(" install -r", fail("adb: failed to install: Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: Package br.tematv.launcher signatures do not match]"))

        val outcome = steps.installLauncher(release)

        assertEquals("assinatura-diferente", outcome.errorCode)
        assertEquals("apk-ausente", steps.installLauncher(null).errorCode)
    }

    @Test
    fun `início automático liberado, sem a opção ou com erro`() {
        runner.on("appops get", ok("APP_AUTO_START: allow\n"))
        assertEquals(StepStatus.OK, steps.allowAutoStart().status)

        runner.on("appops set", fail("Error: Unknown operation string: APP_AUTO_START\n"))
        assertEquals(StepStatus.SKIPPED, steps.allowAutoStart().status)

        runner.on("appops set", ok(""))
        runner.on("appops get", ok("APP_AUTO_START: deny\n"))
        assertEquals(StepStatus.ERROR, steps.allowAutoStart().status)
    }

    @Test
    fun `lê o código do aparelho repetindo enquanto a importação não responde`() {
        runner.on("device_code.txt", fail("Error"), fail("Error"), ok("2544-3298-7734\n"))

        val outcome = steps.readDeviceCode()

        assertEquals("2544-3298-7734", outcome.deviceCode)
        assertEquals(StepStatus.OK, outcome.status)
    }

    @Test
    fun `tema conferido pelo estado do app`() {
        val packageFile = folder.newFile("igreja-x.tematv")
        runner.on("app_status.json", status(theme = null), status(theme = "igreja-x"))

        assertEquals(StepStatus.OK, steps.sendTheme(packageFile, "igreja-x").status)
        assertTrue(runner.stdinWrites.any { (command, input) -> command.contains("active_theme.txt") && input == "igreja-x.tematv\n" })

        runner.on("app_status.json", status(theme = "outro"))
        assertEquals("tema", steps.sendTheme(packageFile, "igreja-x").errorCode)
    }

    @Test
    fun `ativação envia a senha pela entrada padrão e confere pelo estado`() {
        runner.on("app_status.json", status(activated = false), status(activated = false), status(activated = false),
            status(activated = false), status(activated = false), status(activated = false), status(activated = true))

        val outcome = steps.activate("2544-3298-7734", "Igreja X")

        assertEquals(StepStatus.OK, outcome.status)
        val (command, input) = runner.stdinWrites.single { it.first.contains("activation_code.txt") }
        assertTrue(input.trim().matches(Regex("\\d{4}-\\d{4}-\\d{4}")))
        assertFalse(command.contains(input.trim()))
        assertFalse(outcome.message.contains(input.trim()))
        assertTrue(activationLog.readText().contains("2544-3298-7734,Igreja X"))
    }

    @Test
    fun `TV já ativada não gera senha nova`() {
        runner.on("app_status.json", status(activated = true))

        assertEquals(StepStatus.SKIPPED, steps.activate("2544-3298-7734", "Igreja X").status)
        assertFalse(activationLog.exists())
    }

    @Test
    fun `acessibilidade preserva os outros serviços com aspas`() {
        val other = "com.exemplo/com.exemplo.Leitor\$Interno"
        runner.on("settings get secure enabled_accessibility_services", ok("$other\n"))
        runner.on("dumpsys accessibility", ok("Enabled services:{{${AccessibilityList.SERVICE}}}\n"))

        assertEquals(StepStatus.OK, steps.enableTakeover().status)
        assertTrue(runner.commands.contains("adb -s 192.168.1.2:5555 shell settings put secure enabled_accessibility_services '$other:${AccessibilityList.SERVICE}'"))
    }

    @Test
    fun `reinstalar não regrava a lista que já tem o serviço`() {
        runner.on("settings get secure enabled_accessibility_services", ok("${AccessibilityList.SERVICE}\n"))
        runner.on("dumpsys accessibility", ok("Enabled services:{{${AccessibilityList.SERVICE}}}\n"))

        assertEquals(StepStatus.OK, steps.enableTakeover().status)
        assertFalse(runner.commands.any { it.contains("put secure enabled_accessibility_services") })
    }

    @Test
    fun `conferência usa a linha de foco não nula`() {
        runner.on("mCurrentFocus", ok("  mCurrentFocus=null\n  mCurrentFocus=Window{1a2b u0 br.tematv.launcher/br.tematv.launcher.home.HomeActivity}\n"))
        assertEquals(StepStatus.OK, steps.verifyHome().status)

        runner.on("mCurrentFocus", ok("  mCurrentFocus=Window{3c4d u0 com.google.android.apps.tv.launcherx/.MainActivity}\n"))
        val outcome = steps.verifyHome()
        assertEquals("conferencia", outcome.errorCode)
        assertTrue(outcome.message.contains("com.google.android.apps.tv.launcherx"))
    }

    @Test
    fun `desfazer tira o serviço, mantém os outros e desinstala`() {
        val other = "com.exemplo/.Leitor"
        runner.on("settings get secure enabled_accessibility_services", ok("$other:${AccessibilityList.SERVICE}\n"))
        runner.on("uninstall", ok("Success\n"))

        assertEquals(StepStatus.OK, steps.undo().status)
        assertTrue(runner.commands.contains("adb -s 192.168.1.2:5555 shell settings put secure enabled_accessibility_services '$other'"))
        assertFalse(runner.commands.any { it.contains("accessibility_enabled 0") })
    }

    @Test
    fun `desfazer sem outros serviços desliga a acessibilidade`() {
        runner.on("settings get secure enabled_accessibility_services", ok("${AccessibilityList.SERVICE}\n"))
        runner.on("uninstall", fail("Failure [DELETE_FAILED_INTERNAL_ERROR]"))

        assertEquals(StepStatus.SKIPPED, steps.undo().status)
        assertTrue(runner.commands.any { it.contains("accessibility_enabled 0") })
        assertNull(steps.undo().errorCode)
    }
}
