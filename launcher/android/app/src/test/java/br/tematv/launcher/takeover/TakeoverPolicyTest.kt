package br.tematv.launcher.takeover

import org.junit.Assert.assertEquals
import org.junit.Test

class TakeoverPolicyTest {

    private val policy = TakeoverPolicy()
    private val googleTv = SystemLauncherPackages.GOOGLE_TV
    private val now = 1_000_000L

    private fun state(
        enabled: Boolean = true,
        suppressUntil: Long = 0,
        pausedUntil: Long = 0,
        recentOpens: List<Long> = emptyList(),
    ) = TakeoverState(enabled, suppressUntil, pausedUntil, recentOpens)

    private fun ignored(reason: IgnoreReason) = TakeoverDecision.Ignore(reason)

    @Test
    fun opensWhenSystemLauncherAppearsAndTakeoverIsOn() {
        assertEquals(TakeoverDecision.Open, policy.decide(googleTv, state(), nowElapsedMs = now))
    }

    @Test
    fun ignoresPackagesThatAreNotSystemLaunchers() {
        assertEquals(ignored(IgnoreReason.NOT_SYSTEM_LAUNCHER), policy.decide("com.google.android.youtube.tv", state(), now))
        assertEquals(ignored(IgnoreReason.NOT_SYSTEM_LAUNCHER), policy.decide(null, state(), now))
    }

    @Test
    fun ignoresWhenTakeoverIsOff() {
        assertEquals(ignored(IgnoreReason.TAKEOVER_DISABLED), policy.decide(googleTv, state(enabled = false), now))
    }

    @Test
    fun ignoresDuringFiveSecondsAfterDisabling() {
        val suppressed = state(suppressUntil = now + 4_000)

        assertEquals(ignored(IgnoreReason.SUPPRESSED_AFTER_DISABLE), policy.decide(googleTv, suppressed, now))
        assertEquals(TakeoverDecision.Open, policy.decide(googleTv, suppressed, now + 4_001))
    }

    @Test
    fun pausesOnFourthOpenWithinTenSeconds() {
        val threeOpens = listOf(now - 9_000, now - 6_000, now - 3_000)

        assertEquals(TakeoverDecision.PauseForLoop, policy.decide(googleTv, state(recentOpens = threeOpens), now))
    }

    @Test
    fun opensWhenOldOpensLeftTheTenSecondWindow() {
        val opens = listOf(now - 15_000, now - 12_000, now - 3_000)

        assertEquals(TakeoverDecision.Open, policy.decide(googleTv, state(recentOpens = opens), now))
    }

    @Test
    fun groupsBurstOfEventsFromTheSameHomePress() {
        assertEquals(ignored(IgnoreReason.RECENTLY_OPENED), policy.decide(googleTv, state(recentOpens = listOf(now - 400)), now))
    }

    @Test
    fun ignoresWhilePausedAndResumesAfterThirtySeconds() {
        val paused = state(pausedUntil = now + 30_000)

        assertEquals(ignored(IgnoreReason.PAUSED), policy.decide(googleTv, paused, now))
        assertEquals(TakeoverDecision.Open, policy.decide(googleTv, paused, now + 30_001))
    }

    @Test
    fun treatsWindowsFromBeforeRebootAsExpired() {
        // Depois de reiniciar, elapsedRealtime recomeça perto de zero; janelas antigas ficam muito à frente.
        val afterReboot = 40_000L
        val stale = state(suppressUntil = 5_000_000, pausedUntil = 5_000_000)

        assertEquals(TakeoverDecision.Open, policy.decide(googleTv, stale, afterReboot))
    }

    @Test
    fun bootDecisionAppliesTheSameRules() {
        assertEquals(TakeoverDecision.Open, policy.decideForBoot(state(), now))
        assertEquals(ignored(IgnoreReason.TAKEOVER_DISABLED), policy.decideForBoot(state(enabled = false), now))
    }
}
