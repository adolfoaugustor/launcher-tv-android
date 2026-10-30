package br.tematv.themebuilder.install

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityListTest {

    private val service = AccessibilityList.SERVICE
    private val other = "com.exemplo/com.exemplo.Leitor\$Interno"

    @Test
    fun `lista nula ou vazia recebe só o serviço`() {
        assertEquals(service, AccessibilityList.withService("null\n"))
        assertEquals(service, AccessibilityList.withService(""))
    }

    @Test
    fun `acrescenta sem tirar os outros nem duplicar`() {
        assertEquals("$other:$service", AccessibilityList.withService("$other\n"))
        assertEquals("$other:$service", AccessibilityList.withService("$other:$service"))
        assertEquals("$other:br.tematv.launcher/.takeover.HomeTakeoverService", AccessibilityList.withService("$other:br.tematv.launcher/.takeover.HomeTakeoverService"))
    }

    @Test
    fun `compara o componente inteiro, não um trecho`() {
        val lookalike = "$service" + "Falso"
        assertFalse(AccessibilityList.contains(lookalike))
        assertEquals("$lookalike:$service", AccessibilityList.withService(lookalike))
        assertTrue(AccessibilityList.contains("br.tematv.launcher/.takeover.HomeTakeoverService"))
    }

    @Test
    fun `remove só o serviço`() {
        assertEquals(other, AccessibilityList.withoutService("$other:$service"))
        assertEquals("", AccessibilityList.withoutService(service))
    }

    @Test
    fun `aspas simples protegem cifrão, espaço e apóstrofo`() {
        assertEquals("'com.x/.A\$B'", AccessibilityList.shellQuote("com.x/.A\$B"))
        assertEquals("'a'\\''b c'", AccessibilityList.shellQuote("a'b c"))
    }
}
