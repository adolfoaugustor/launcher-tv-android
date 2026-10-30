package br.tematv.themebuilder.install

/**
 * Lista `enabled_accessibility_services` da TV (D-11): compara componentes inteiros
 * (`pacote/.Classe` e `pacote/pacote.Classe` são o mesmo) e preserva os serviços dos outros apps (RN-02).
 */
object AccessibilityList {

    const val SERVICE = "br.tematv.launcher/br.tematv.launcher.takeover.HomeTakeoverService"

    fun parse(raw: String): List<String> =
        raw.trim().takeUnless { it.isEmpty() || it == "null" }?.split(':')?.filter { it.isNotBlank() }.orEmpty()

    fun contains(raw: String): Boolean = parse(raw).any { normalize(it) == SERVICE }

    fun withService(raw: String): String {
        val services = parse(raw)
        return if (services.any { normalize(it) == SERVICE }) services.joinToString(":") else (services + SERVICE).joinToString(":")
    }

    fun withoutService(raw: String): String = parse(raw).filterNot { normalize(it) == SERVICE }.joinToString(":")

    /** Valor entre aspas simples para o `sh` da TV não expandir `$` nem separar espaços. */
    fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

    private fun normalize(component: String): String {
        val packageName = component.substringBefore('/')
        val className = component.substringAfter('/', "")
        return if (className.startsWith('.')) "$packageName/$packageName$className" else component
    }
}
