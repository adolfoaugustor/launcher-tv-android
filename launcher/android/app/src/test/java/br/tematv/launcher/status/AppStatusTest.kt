package br.tematv.launcher.status

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class AppStatusTest {

    private val status = AppStatus(
        versionCode = 6,
        versionName = "0.6.0",
        signerSha256 = "235db11a",
        activated = true,
        activeThemeId = "igreja-batista-x",
        updatedAt = Instant.parse("2026-10-30T20:00:00Z"),
    )

    @Test
    fun serializesAllFieldsInTheContractFormat() {
        assertEquals(
            "{\"version_code\":6,\"version_name\":\"0.6.0\",\"signer_sha256\":\"235db11a\",\"activated\":true," +
                "\"active_theme_id\":\"igreja-batista-x\",\"updated_at\":\"2026-10-30T20:00:00Z\"}",
            status.toJson(),
        )
    }

    @Test
    fun defaultThemeIsNullAndTextIsEscaped() {
        val json = status.copy(activated = false, activeThemeId = null, versionName = "0.6 \"beta\"\n").toJson()

        assertEquals(true, json.contains("\"activated\":false"))
        assertEquals(true, json.contains("\"active_theme_id\":null"))
        assertEquals(true, json.contains("\"version_name\":\"0.6 \\\"beta\\\"\\u000a\""))
    }
}
