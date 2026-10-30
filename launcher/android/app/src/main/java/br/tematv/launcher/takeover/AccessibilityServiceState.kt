package br.tematv.launcher.takeover

import android.content.Context
import android.provider.Settings

/** Se o serviço de troca está ligado, lido das configurações seguras do Android (D-09). */
object AccessibilityServiceState {

    fun isServiceEnabled(context: Context): Boolean {
        val resolver = context.contentResolver
        val accessibilityEnabled = Settings.Secure.getInt(resolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1
        val enabledServices = Settings.Secure.getString(resolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return accessibilityEnabled &&
            isListed(enabledServices, context.packageName, HomeTakeoverService::class.java.name)
    }

    /** Interpreta a lista `pacote/Classe:pacote/.Classe` de `enabled_accessibility_services`. */
    fun isListed(enabledServicesSetting: String?, packageName: String, className: String): Boolean {
        if (enabledServicesSetting.isNullOrBlank() || enabledServicesSetting == "null") return false
        return enabledServicesSetting.split(':').any { entry ->
            val entryPackage = entry.trim().substringBefore('/')
            val entryClass = entry.trim().substringAfter('/', missingDelimiterValue = "")
            val fullClassName = if (entryClass.startsWith(".")) entryPackage + entryClass else entryClass
            entryPackage == packageName && fullClassName == className
        }
    }
}
