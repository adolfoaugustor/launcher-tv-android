package br.tematv.launcher.takeover

/** Consultado antes de cada troca (RF-12). A feature 004 troca a implementação pela validação real. */
fun interface LicenseGate {
    fun isLicenseValid(): Boolean
}

/** Licença sempre válida até a feature 004 existir (RN-05). */
object AlwaysValidLicenseGate : LicenseGate {
    override fun isLicenseValid(): Boolean = true
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
