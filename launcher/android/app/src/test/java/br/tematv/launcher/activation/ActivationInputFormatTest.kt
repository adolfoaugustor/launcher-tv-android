package br.tematv.launcher.activation

import org.junit.Assert.assertEquals
import org.junit.Test

class ActivationInputFormatTest {

    @Test
    fun groupsDigitsInFoursWhileTyping() {
        assertEquals("", ActivationInputFormat.format(""))
        assertEquals("806", ActivationInputFormat.format("806"))
        assertEquals("8065", ActivationInputFormat.format("8065"))
        assertEquals("8065-4", ActivationInputFormat.format("80654"))
        assertEquals("8065-4698-2216", ActivationInputFormat.format("806546982216"))
    }

    @Test
    fun deletingAfterAGroupDropsTheDash() {
        assertEquals("8065", ActivationInputFormat.format("8065-"))
    }

    @Test
    fun keepsOnlyTwelveDigitsAndIgnoresOtherCharacters() {
        assertEquals("8065-4698-2216", ActivationInputFormat.format("8065-4698-22169"))
        assertEquals("8065-4698-2216", ActivationInputFormat.format(" 8065 4698.2216 "))
    }
}
