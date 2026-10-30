package br.tematv.activation

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Código do aparelho e senha de ativação: 12 números cada, exibidos como `XXXX-XXXX-XXXX`.
 * Algoritmo descrito em `_reversa_forward/004-license-activation/interfaces/senha-ativacao.md`.
 */
object ActivationDigits {
    const val LENGTH = 12
    private val MODULUS = 1_000_000_000_000uL
    private val ALLOWED_SEPARATORS = setOf(' ', '-', '.')

    /** Tira separadores; devolve os 12 dígitos ou nulo se a entrada não for exatamente 12 números. */
    fun normalize(input: String): String? {
        if (input.any { !it.isDigit() && it !in ALLOWED_SEPARATORS }) return null
        return input.filter(Char::isDigit).takeIf { it.length == LENGTH }
    }

    fun format(digits: String): String = digits.chunked(4).joinToString("-")

    /** Primeiros 8 bytes como inteiro sem sinal, `mod 10^12`, com zeros à esquerda. */
    internal fun fromHash(hash: ByteArray): String =
        (ByteBuffer.wrap(hash, 0, 8).long.toULong() % MODULUS).toString().padStart(LENGTH, '0')
}

object DeviceCode {
    fun fromAndroidId(androidId: String, applicationId: String): String {
        val hash = MessageDigest.getInstance("SHA-256").digest("$androidId:$applicationId".toByteArray(Charsets.UTF_8))
        return ActivationDigits.fromHash(hash)
    }
}

object ActivationCode {
    private const val SECRET_BYTES = 32
    private const val MAC_ALGORITHM = "HmacSHA256"

    fun generateSecretHex(random: SecureRandom = SecureRandom()): String =
        ByteArray(SECRET_BYTES).also(random::nextBytes).joinToString("") { "%02x".format(it) }

    /** Senha esperada para o código do aparelho; falha com mensagem clara para segredo ou código inválidos. */
    fun expectedFor(secretHex: String, deviceCode: String): String {
        val digits = requireNotNull(ActivationDigits.normalize(deviceCode)) { "código do aparelho inválido: $deviceCode" }
        val mac = Mac.getInstance(MAC_ALGORITHM).apply { init(SecretKeySpec(parseSecret(secretHex), MAC_ALGORITHM)) }
        return ActivationDigits.fromHash(mac.doFinal(digits.toByteArray(Charsets.UTF_8)))
    }

    /** Compara em tempo constante para não revelar acertos parciais. */
    fun matches(secretHex: String, deviceCode: String, typedCode: String): Boolean {
        val typed = ActivationDigits.normalize(typedCode) ?: return false
        val expected = expectedFor(secretHex, deviceCode)
        return MessageDigest.isEqual(typed.toByteArray(Charsets.US_ASCII), expected.toByteArray(Charsets.US_ASCII))
    }

    private fun parseSecret(secretHex: String): ByteArray {
        val clean = secretHex.trim()
        require(clean.length == SECRET_BYTES * 2 && clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            "segredo de ativação inválido: esperados ${SECRET_BYTES * 2} caracteres hexadecimais"
        }
        return clean.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
