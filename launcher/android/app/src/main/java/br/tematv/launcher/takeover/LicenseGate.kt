package br.tematv.launcher.takeover

/** Se a TV está ativada (feature 004); implementado por `ActivationLicenseGate`. */
fun interface LicenseGate {
    fun isLicenseValid(): Boolean
}

/** Launchers do sistema observados pela troca (D-02); a mesma lista está em `res/xml/home_takeover_service.xml`. */
object SystemLauncherPackages {
    const val GOOGLE_TV = "com.google.android.apps.tv.launcherx"

    val packageNames: Set<String> = setOf(
        GOOGLE_TV,
        "com.google.android.tvlauncher",
        "com.google.android.leanbacklauncher",
    )
}
