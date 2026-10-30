package br.tematv.launcher.theme

/** Nomes que a importação pelo ADB aceita gravar na pasta de temas (contrato `importacao-adb.md`). */
object ThemeImportNames {

    private val packageNamePattern = Regex("^[a-z0-9-]+\\.tematv$")

    fun isAllowed(fileName: String): Boolean =
        fileName == ThemeStorage.ACTIVE_THEME_FILE_NAME || packageNamePattern.matches(fileName)

    /** Nome do arquivo quando a URI tem exatamente um segmento aceito; nulo nos demais casos. */
    fun fileNameFrom(pathSegments: List<String>): String? =
        pathSegments.singleOrNull()?.takeIf(::isAllowed)
}
