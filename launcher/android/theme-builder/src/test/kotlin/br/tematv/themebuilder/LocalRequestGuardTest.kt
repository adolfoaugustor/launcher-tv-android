package br.tematv.themebuilder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LocalRequestGuardTest {

    private val guard = LocalRequestGuard(8787)

    @Test
    fun `aceita localhost e 127 0 0 1 na porta da ferramenta`() {
        assertNull(guard.rejectionReason("GET", "localhost:8787", null, null))
        assertNull(guard.rejectionReason("GET", "127.0.0.1:8787", null, null))
        assertNull(guard.rejectionReason("POST", "localhost:8787", "http://localhost:8787", "application/json; charset=utf-8"))
    }

    @Test
    fun `recusa outro Host`() {
        listOf("evil.example:8787", "localhost:8788", "localhost", null).forEach { host ->
            assertEquals("Host não permitido", guard.rejectionReason("GET", host, null, null))
        }
    }

    @Test
    fun `recusa escrita sem JSON`() {
        assertNotNull(guard.rejectionReason("POST", "localhost:8787", null, "text/plain"))
        assertNotNull(guard.rejectionReason("PUT", "localhost:8787", null, null))
    }

    @Test
    fun `recusa Origin de outro endereço e aceita Origin ausente`() {
        assertEquals("Origem não permitida", guard.rejectionReason("POST", "localhost:8787", "http://evil.example", "application/json"))
        assertEquals("Origem não permitida", guard.rejectionReason("DELETE", "localhost:8787", "null", null))
        assertNull(guard.rejectionReason("DELETE", "localhost:8787", null, null))
        assertNull(guard.rejectionReason("POST", "localhost:8787", null, "application/json"))
    }
}
