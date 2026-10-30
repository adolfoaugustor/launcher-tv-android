package br.tematv.launcher.home

import org.junit.Assert.assertEquals
import org.junit.Test

class AppListBuilderTest {

    private val builder = AppListBuilder(ownPackageName = "br.tematv.launcher")

    private fun app(packageName: String, label: String) = LaunchableApp(packageName, "$packageName.Main", label)

    @Test
    fun removesOwnPackageAndHiddenSystemUtilities() {
        val apps = listOf(
            app("br.tematv.launcher", "Launcher Temático"),
            app("com.tcl.guard", "Guard"),
            app("com.tcl.suspension", "Suspension"),
            app("com.tcl.partnercustomizer", "Partner Customizer"),
            app("com.tcl.notereminder", "Note Reminder"),
            app("com.tcl.dashboard", "Dashboard"),
            app("com.tcl.waterfall.overseas", "Waterfall"),
            app("com.tcl.t_solo", "T-Solo"),
            app("com.tcl.tv", "TV"),
            app("com.google.android.youtube.tv", "YouTube"),
        )

        val packages = builder.build(apps).map { it.packageName }

        assertEquals(listOf("com.tcl.tv", "com.google.android.youtube.tv"), packages)
    }

    @Test
    fun sortsAlphabeticallyIgnoringCaseAndAccents() {
        val apps = listOf(
            app("com.google.android.youtube.tv", "YouTube"),
            app("com.netflix.ninja", "Netflix"),
            app("com.disney.disneyplus", "Disney+"),
            app("com.example.otica", "Ótica"),
            app("com.example.ambar", "ambar"),
        )

        val labels = builder.build(apps).map { it.label }

        assertEquals(listOf("ambar", "Disney+", "Netflix", "Ótica", "YouTube"), labels)
    }

    @Test
    fun keepsEqualLabelsInPackageOrder() {
        val apps = listOf(app("com.b.album", "Álbum"), app("com.a.album", "album"))

        val packages = builder.build(apps).map { it.packageName }

        assertEquals(listOf("com.a.album", "com.b.album"), packages)
    }

    @Test
    fun returnsEmptyListWhenOnlyHiddenAppsExist() {
        assertEquals(emptyList<LaunchableApp>(), builder.build(listOf(app("com.tcl.guard", "Guard"))))
    }
}
