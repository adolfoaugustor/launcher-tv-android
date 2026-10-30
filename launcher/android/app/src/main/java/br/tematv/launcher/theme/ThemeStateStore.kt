package br.tematv.launcher.theme

import android.content.Context
import java.io.File

/** Último erro de pacote, para a tela de diagnóstico da feature 2. */
data class RecordedThemeError(
    val code: String,
    val field: String?,
    val packageName: String?,
    val recordedAt: String,
)

/** Estado que sobrevive a reinicializações: último tema válido e último erro. */
interface ThemeStatePersistence {
    val lastValidPackage: String?
    val lastValidCacheDir: File?
    val lastError: RecordedThemeError?

    fun saveLastValid(packageName: String, cacheDir: File)

    fun saveError(error: RecordedThemeError)
}

class SharedPreferencesThemeState(context: Context) : ThemeStatePersistence {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override val lastValidPackage: String?
        get() = preferences.getString(KEY_LAST_VALID_PACKAGE, null)

    override val lastValidCacheDir: File?
        get() = preferences.getString(KEY_LAST_VALID_CACHE_DIR, null)?.let(::File)

    override val lastError: RecordedThemeError?
        get() {
            val code = preferences.getString(KEY_LAST_ERROR_CODE, null) ?: return null
            return RecordedThemeError(
                code = code,
                field = preferences.getString(KEY_LAST_ERROR_FIELD, null),
                packageName = preferences.getString(KEY_LAST_ERROR_PACKAGE, null),
                recordedAt = preferences.getString(KEY_LAST_ERROR_AT, null).orEmpty(),
            )
        }

    override fun saveLastValid(packageName: String, cacheDir: File) {
        preferences.edit()
            .putString(KEY_LAST_VALID_PACKAGE, packageName)
            .putString(KEY_LAST_VALID_CACHE_DIR, cacheDir.path)
            .commit()
    }

    override fun saveError(error: RecordedThemeError) {
        preferences.edit()
            .putString(KEY_LAST_ERROR_CODE, error.code)
            .putString(KEY_LAST_ERROR_FIELD, error.field)
            .putString(KEY_LAST_ERROR_PACKAGE, error.packageName)
            .putString(KEY_LAST_ERROR_AT, error.recordedAt)
            .commit()
    }

    private companion object {
        const val PREFERENCES_NAME = "theme_state"
        const val KEY_LAST_VALID_PACKAGE = "last_valid_package"
        const val KEY_LAST_VALID_CACHE_DIR = "last_valid_cache_dir"
        const val KEY_LAST_ERROR_CODE = "last_error_code"
        const val KEY_LAST_ERROR_FIELD = "last_error_field"
        const val KEY_LAST_ERROR_PACKAGE = "last_error_package"
        const val KEY_LAST_ERROR_AT = "last_error_at"
    }
}
