package br.tematv.launcher.theme

/** Nomes que a importação pelo ADB aceita gravar na pasta de temas (contrato `importacao-adb.md`). */
object ThemeImportNames {

    private val packageNamePattern = Regex("^[a-z0-9-]+\\.tematv$")

    const val DEVICE_CODE_FILE_NAME = "device_code.txt"
    const val ACTIVATION_CODE_FILE_NAME = "activation_code.txt"
    const val APP_STATUS_FILE_NAME = "app_status.json"

    private val readOnlyNames = setOf(APP_STATUS_FILE_NAME)
    private val fixedNames = setOf(ThemeStorage.ACTIVE_THEME_FILE_NAME, DEVICE_CODE_FILE_NAME, ACTIVATION_CODE_FILE_NAME) + readOnlyNames

    fun isAllowed(fileName: String): Boolean = fileName in fixedNames || packageNamePattern.matches(fileName)

    /** O estado do app é só para leitura pelo ADB. */
    fun isWritable(fileName: String): Boolean = fileName !in readOnlyNames

    /** Nome do arquivo quando a URI tem exatamente um segmento aceito; nulo nos demais casos. */
    fun fileNameFrom(pathSegments: List<String>): String? =
        pathSegments.singleOrNull()?.takeIf(::isAllowed)
}
