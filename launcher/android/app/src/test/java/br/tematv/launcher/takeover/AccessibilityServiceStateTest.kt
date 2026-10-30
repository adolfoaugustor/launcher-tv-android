package br.tematv.launcher.takeover

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityServiceStateTest {

    private val packageName = "br.tematv.launcher"
    private val className = "br.tematv.launcher.takeover.HomeTakeoverService"

    private fun listed(setting: String?) = AccessibilityServiceState.isListed(setting, packageName, className)

    @Test
    fun emptyOrNullSettingMeansOff() {
        assertFalse(listed(null))
        assertFalse(listed(""))
        assertFalse(listed("null"))
    }

    @Test
    fun findsOurServiceAloneOrAmongOthers() {
        assertTrue(listed("br.tematv.launcher/br.tematv.launcher.takeover.HomeTakeoverService"))
        assertTrue(listed("com.outro/com.outro.Servico:br.tematv.launcher/br.tematv.launcher.takeover.HomeTakeoverService"))
    }

    @Test
    fun acceptsShortClassForm() {
        assertTrue(listed("br.tematv.launcher/.takeover.HomeTakeoverService"))
    }

    @Test
    fun ignoresSimilarButDifferentEntries() {
        assertFalse(listed("br.tematv.launcher/br.tematv.launcher.takeover.OutroServico"))
        assertFalse(listed("br.tematv.launcherx/br.tematv.launcher.takeover.HomeTakeoverService"))
        assertFalse(listed("com.outro/com.outro.Servico"))
    }
}
