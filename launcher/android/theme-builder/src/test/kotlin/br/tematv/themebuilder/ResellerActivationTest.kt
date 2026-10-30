package br.tematv.themebuilder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ResellerActivationTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC)
    private val secretFile by lazy { File(folder.root, "activation-secret").apply { writeText(SECRET + "\n") } }
    private val logFile by lazy { File(folder.root, "tematv/ativacoes.csv") }
    private val activation by lazy { ResellerActivation(secretFile, logFile, clock) }

    @Test
    fun `reproduz os vetores fixos do activation-core`() {
        assertEquals(GeneratedActivation("3675-9161-9596", "0142-5497-2025"), activation.generate("367591619596", ""))
        assertEquals("6382-8378-1490", activation.generate("9034-6012-4593", "").activationCode)
        assertEquals("8040-0158-1742", activation.generate(" 0000 0000 0000 ", "").activationCode)
    }

    @Test
    fun `registra cada senha no mesmo formato do activation-tool`() {
        activation.generate("3675-9161-9596", "Igreja Batista X")
        activation.generate("3675-9161-9596", "Loja \"Zé\", centro")

        assertEquals(
            listOf(
                "2026-10-30T12:00:00Z,3675-9161-9596,Igreja Batista X",
                "2026-10-30T12:00:00Z,3675-9161-9596,\"Loja \"\"Zé\"\", centro\"",
            ),
            logFile.readLines(),
        )
    }

    @Test
    fun `código com dígitos a menos é recusado sem registrar`() {
        val error = assertThrows(UserInputException::class.java) { activation.generate("3675-9161-959", "") }

        assertEquals("device_code", error.field)
        assertFalse(logFile.exists())
    }

    @Test
    fun `sem o segredo responde 409 e não mostra o segredo`() {
        val missing = ResellerActivation(File(folder.root, "nao-existe"), logFile, clock)

        val error = assertThrows(UserInputException::class.java) { missing.generate("367591619596", "") }

        assertFalse(missing.isSecretAvailable)
        assertEquals(409, error.httpStatus)
        assertFalse(error.message!!.contains(SECRET))
    }

    private companion object {
        const val SECRET = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"
    }
}
