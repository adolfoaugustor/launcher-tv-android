package br.tematv.launcher.home

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

/** App pronto para a grade: banner de TV, ou ícone quando o app não tem banner. */
data class AppEntry(val app: LaunchableApp, val artwork: Drawable, val hasBanner: Boolean) {
    val packageName: String get() = app.packageName
    val label: String get() = app.label
}

/** Lê os apps com lançador de TV instalados (D-05, D-06). Chamar fora da thread principal. */
class InstalledAppsSource(private val context: Context) {

    private val packageManager = context.packageManager
    private val appListBuilder = AppListBuilder(ownPackageName = context.packageName)

    fun load(): List<AppEntry> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val resolvedActivities = packageManager.queryIntentActivities(launcherIntent, 0)
            .associateBy { it.activityInfo.packageName to it.activityInfo.name }
        val launchableApps = resolvedActivities.values.map { resolved ->
            LaunchableApp(
                packageName = resolved.activityInfo.packageName,
                activityName = resolved.activityInfo.name,
                label = resolved.loadLabel(packageManager).toString(),
            )
        }
        return appListBuilder.build(launchableApps).map { app ->
            val activityInfo = resolvedActivities.getValue(app.packageName to app.activityName).activityInfo
            val banner = activityInfo.loadBanner(packageManager) ?: activityInfo.applicationInfo.loadBanner(packageManager)
            AppEntry(app, artwork = banner ?: activityInfo.loadIcon(packageManager), hasBanner = banner != null)
        }
    }
}
