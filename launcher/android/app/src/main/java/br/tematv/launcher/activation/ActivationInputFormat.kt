package br.tematv.launcher.activation

import android.text.Editable
import android.text.TextWatcher
import br.tematv.activation.ActivationDigits

/** Campo da senha no formato `0000-0000-0000`: o teclado numérico da TV não tem traço (emenda E001 da feature 004). */
object ActivationInputFormat {

    private const val GROUP_SIZE = 4

    fun format(typed: CharSequence): String =
        typed.filter(Char::isDigit).take(ActivationDigits.LENGTH).chunked(GROUP_SIZE).joinToString("-")
}

/** Reaplica [ActivationInputFormat] a cada tecla, deixando o cursor no fim. */
class ActivationInputWatcher : TextWatcher {

    private var formatting = false

    override fun afterTextChanged(editable: Editable) {
        if (formatting) return
        val formatted = ActivationInputFormat.format(editable)
        if (formatted == editable.toString()) return
        formatting = true
        editable.replace(0, editable.length, formatted)
        formatting = false
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) = Unit
}
