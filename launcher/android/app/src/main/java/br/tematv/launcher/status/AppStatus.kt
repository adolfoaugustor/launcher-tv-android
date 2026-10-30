package br.tematv.launcher.status

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import br.tematv.launcher.theme.ThemeImportNames
import br.tematv.launcher.theme.ThemeStorage
import java.io.IOException
import java.security.MessageDigest
import java.time.Instant

/** Estado lido pela instalação no notebook (`interfaces/estado-do-app.md` da feature 006). */
data class AppStatus(
    val versionCode: Long,
    val versionName: String,
    val signerSha256: String,
    val activated: Boolean,
    val activeThemeId: String?,
    val updatedAt: Instant,
) {
    fun toJson(): String = listOf(
        "\"version_code\":$versionCode",
        "\"version_name\":${quote(versionName)}",
        "\"signer_sha256\":${quote(signerSha256)}",
        "\"activated\":$activated",
        "\"active_theme_id\":${activeThemeId?.let(::quote) ?: "null"}",
        "\"updated_at\":${quote(updatedAt.toString())}",
    ).joinToString(",", prefix = "{", postfix = "}")

    private fun quote(value: String): String = buildString {
        append('"')
        value.forEach { char ->
            when {
                char == '"' || char == '\\' -> append('\\').append(char)
                char < ' ' -> append("\\u%04x".format(char.code))
                else -> append(char)
            }
        }
        append('"')
    }
}

/** Grava `app_status.json` na pasta de importação, para o ADB ler (D-04). */
class AppStatusPublisher(private val context: Context) {

    fun publish(storage: ThemeStorage, activated: Boolean, activeThemeId: String?) {
        val status = AppStatus(
            versionCode = packageInfo.longVersionCode,
            versionName = packageInfo.versionName.orEmpty(),
            signerSha256 = signerSha256,
            activated = activated,
            activeThemeId = activeThemeId,
            updatedAt = Instant.now(),
        )
        try {
            if (!storage.ensureThemesDir()) return
            storage.packageFile(ThemeImportNames.APP_STATUS_FILE_NAME)?.writeText(status.toJson() + "\n")
        } catch (exception: IOException) {
            Log.w(LOG_TAG, "Estado do app: não foi possível gravar: ${exception.message}")
        }
    }

    private val packageInfo by lazy {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
    }

    private val signerSha256: String by lazy {
        val certificate = packageInfo.signingInfo.apkContentsSigners.first().toByteArray()
        MessageDigest.getInstance("SHA-256").digest(certificate).joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val LOG_TAG = "TemaTV"
    }
}
