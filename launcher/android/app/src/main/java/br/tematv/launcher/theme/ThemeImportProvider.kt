package br.tematv.launcher.theme

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.ParcelFileDescriptor
import java.io.FileNotFoundException

/**
 * Entrada de importação de temas pelo ADB: `adb shell 'content write --uri content://br.tematv.launcher.themes/<nome>' < arquivo`.
 * Existe porque, na TCL com Android 11, o ADB não tem permissão de gravar em `Android/data/<pacote>/`.
 * Só aceita chamadas do shell do ADB ou do root (contrato `importacao-adb.md`).
 */
class ThemeImportProvider : ContentProvider() {

    // O provider é criado antes do Application.onCreate: nada do repositório é usado aqui.
    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        requireAdbCaller()
        val fileName = ThemeImportNames.fileNameFrom(uri.pathSegments)
            ?: throw FileNotFoundException("nome não aceito na importação: ${uri.path}")
        val storage = ThemeStorage.fromContext(requireNotNull(context))
        if (!storage.ensureThemesDir()) throw FileNotFoundException("armazenamento externo do app indisponível")
        val target = storage.packageFile(fileName) ?: throw FileNotFoundException("nome não aceito na importação: $fileName")

        // O significado de "w" varia entre versões do Android; a escrita sempre substitui o arquivo inteiro.
        val isReadOnly = mode == "r"
        val fileMode = if (isReadOnly) {
            ParcelFileDescriptor.MODE_READ_ONLY
        } else {
            ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
        }
        return ParcelFileDescriptor.open(target, fileMode)
    }

    private fun requireAdbCaller() {
        val callingUid = Binder.getCallingUid()
        if (callingUid != SHELL_UID && callingUid != ROOT_UID) {
            throw SecurityException("importação de temas aceita só o ADB (uid $callingUid recusado)")
        }
    }

    override fun getType(uri: Uri): String? = null

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor = throw UnsupportedOperationException("use content write")

    override fun insert(uri: Uri, values: ContentValues?): Uri = throw UnsupportedOperationException("use content write")

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("use content write")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("use content write")

    private companion object {
        // android.os.Process.SHELL_UID não é público na API 28; 2000 é o uid fixo do shell do ADB.
        const val SHELL_UID = 2000
        const val ROOT_UID = 0
    }
}
