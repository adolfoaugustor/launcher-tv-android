package br.tematv.launcher.home

import java.text.Collator
import java.util.Locale

/** App com atividade de lançador de TV, antes de carregar banner e ícone. */
data class LaunchableApp(val packageName: String, val activityName: String, val label: String)

/** Utilitários técnicos de fabricante que ficam fora da grade (RN-06); continuam nas Configurações da TV. */
object HiddenSystemApps {
    val packageNames: Set<String> = setOf(
        "com.tcl.guard",
        "com.tcl.suspension",
        "com.tcl.partnercustomizer",
        "com.tcl.notereminder",
        "com.tcl.dashboard",
        "com.tcl.waterfall.overseas",
        "com.tcl.t_solo",
    )
}

/** Filtra e ordena os apps da grade: alfabética, ignorando acentos e maiúsculas. */
class AppListBuilder(
    private val ownPackageName: String,
    private val hiddenPackageNames: Set<String> = HiddenSystemApps.packageNames,
    locale: Locale = Locale("pt", "BR"),
) {

    private val labelComparator = Collator.getInstance(locale).apply { strength = Collator.PRIMARY }

    fun build(apps: List<LaunchableApp>): List<LaunchableApp> = apps
        .filter { it.packageName != ownPackageName && it.packageName !in hiddenPackageNames }
        .sortedWith(compareBy<LaunchableApp, String>(labelComparator) { it.label }.thenBy { it.packageName })
}
