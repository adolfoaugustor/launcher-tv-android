package br.tematv.activation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

class ActivationCodeTest {

    private val secret = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"

    /**
     * Vetores fixos do contrato `interfaces/senha-ativacao.md`, calculados de forma independente em Python.
     * Qualquer outra implementação do algoritmo precisa reproduzi-los; mudar estes valores invalida senhas já entregues.
     */
    @Test
    fun reproducesFixedVectors() {
        assertEquals("367591619596", DeviceCode.fromAndroidId("0123456789abcdef", "br.tematv.launcher"))
        assertEquals("903460124593", DeviceCode.fromAndroidId("a1b2c3d4e5f60718", "br.tematv.launcher"))
        assertEquals("014254972025", ActivationCode.expectedFor(secret, "367591619596"))
        assertEquals("638283781490", ActivationCode.expectedFor(secret, "903460124593"))
        assertEquals("804001581742", ActivationCode.expectedFor(secret, "000000000000"))
    }

    @Test
    fun formatsInGroupsOfFour() {
        assertEquals("0142-5497-2025", ActivationDigits.format("014254972025"))
    }

    @Test
    fun acceptsSeparatorsAndRejectsOtherCharacters() {
        assertEquals("014254972025", ActivationDigits.normalize("0142-5497-2025"))
        assertEquals("014254972025", ActivationDigits.normalize(" 0142 5497 2025 "))
        assertNull(ActivationDigits.normalize("0142-5497-202"))
        assertNull(ActivationDigits.normalize("0142-5497-20255"))
        assertNull(ActivationDigits.normalize("0142-5497-202a"))
    }

    @Test
    fun matchesOnlyTheCodeOfItsOwnDevice() {
        assertTrue(ActivationCode.matches(secret, "3675-9161-9596", "0142-5497-2025"))
        assertFalse(ActivationCode.matches(secret, "9034-6012-4593", "0142-5497-2025"))
        assertFalse(ActivationCode.matches(secret, "3675-9161-9596", "0142-5497-2026"))
        assertFalse(ActivationCode.matches(secret, "3675-9161-9596", "texto"))
    }

    @Test
    fun differentSecretsGiveDifferentCodes() {
        val otherSecret = "ff" + secret.drop(2)

        assertNotEquals(ActivationCode.expectedFor(secret, "367591619596"), ActivationCode.expectedFor(otherSecret, "367591619596"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidSecret() {
        ActivationCode.expectedFor("curto", "367591619596")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidDeviceCode() {
        ActivationCode.expectedFor(secret, "123")
    }

    @Test
    fun generatesSixtyFourHexCharacterSecrets() {
        val generated = ActivationCode.generateSecretHex(SecureRandom())

        assertEquals(64, generated.length)
        assertTrue(generated.all { it in '0'..'9' || it in 'a'..'f' })
        assertEquals(12, ActivationCode.expectedFor(generated, "367591619596").length)
    }
}
