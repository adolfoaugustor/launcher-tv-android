package br.tematv.themebuilder.install

import br.tematv.themebuilder.ProjectStore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

@Serializable
data class InstallLogEntry(
    val timestamp: String,
    @SerialName("install_id") val installId: String,
    @SerialName("client_theme_id") val clientThemeId: String,
    @SerialName("tv_ip") val tvIp: String,
    @SerialName("tv_manufacturer") val tvManufacturer: String? = null,
    @SerialName("tv_model") val tvModel: String? = null,
    @SerialName("android_version") val androidVersion: String? = null,
    @SerialName("device_code") val deviceCode: String? = null,
    val step: String,
    val result: String,
    val detail: String,
    @SerialName("elapsed_ms") val elapsedMs: Long,
)

/** Uma linha JSON por etapa em `<dir>/<AAAA-MM-DD>-<ip>.log`, sem senha nem segredo (D-15, RN-05). */
class InstallLog(private val dir: File, private val clock: Clock = Clock.systemDefaultZone()) {

    private val json = Json(ProjectStore.json) { prettyPrint = false }

    fun fileFor(ip: String): File = File(dir, "${LocalDate.now(clock)}-$ip.log")

    @Synchronized
    fun append(entry: InstallLogEntry) {
        dir.mkdirs()
        val line = json.encodeToString(InstallLogEntry.serializer(), entry.copy(timestamp = Instant.now(clock).toString()))
        fileFor(entry.tvIp).appendText(line + "\n")
    }
}
