package br.tematv.themebuilder.install

import br.tematv.themebuilder.ProjectStore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class LauncherReleaseInfo(
    @SerialName("version_code") val versionCode: Long,
    @SerialName("version_name") val versionName: String,
    @SerialName("signer_sha256") val signerSha256: String? = null,
)

class LauncherRelease(val apk: File, val info: LauncherReleaseInfo) {
    companion object {
        const val APK_FILE_NAME = "tematv-launcher.apk"
        const val INFO_FILE_NAME = "launcher.json"

        /** APK de produção preparado pelo `tools/instalar-tematv.sh` (D-07); nulo se ainda não existe. */
        fun find(launcherDir: File): LauncherRelease? {
            val apk = File(launcherDir, APK_FILE_NAME)
            val info = File(launcherDir, INFO_FILE_NAME)
            if (!apk.isFile || !info.isFile) return null
            return LauncherRelease(apk, ProjectStore.json.decodeFromString(LauncherReleaseInfo.serializer(), info.readText()))
        }
    }
}
