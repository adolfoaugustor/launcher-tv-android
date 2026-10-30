package br.tematv.activation

import org.junit.Assert.assertEquals
import org.junit.Test

class ActivationAttemptsTest {

    private val now = 1_000_000L

    private fun failTimes(times: Int): AttemptsState =
        (1..times).fold(ActivationAttempts.NONE) { state, _ -> ActivationAttempts.afterFailure(state, now) }

    @Test
    fun fourFailuresDoNotLock() {
        val state = failTimes(4)

        assertEquals(4, state.failedAttempts)
        assertEquals(0, ActivationAttempts.remainingLockMs(state, now))
    }

    @Test
    fun fifthFailureLocksForThirtySeconds() {
        val state = failTimes(5)

        assertEquals(30_000, ActivationAttempts.remainingLockMs(state, now))
        assertEquals(0, state.failedAttempts)
    }

    @Test
    fun lockExpires() {
        val state = failTimes(5)

        assertEquals(1, ActivationAttempts.remainingLockMs(state, now + 29_999))
        assertEquals(0, ActivationAttempts.remainingLockMs(state, now + 30_000))
    }

    @Test
    fun lockFromBeforeRebootCountsAsExpired() {
        val staleLock = AttemptsState(failedAttempts = 0, lockedUntilElapsedMs = 9_000_000)

        assertEquals(0, ActivationAttempts.remainingLockMs(staleLock, nowElapsedMs = 40_000))
    }

    @Test
    fun noAttemptsMeansNoLock() {
        assertEquals(0, ActivationAttempts.remainingLockMs(ActivationAttempts.NONE, now))
    }
}
