package br.tematv.launcher.theme

import android.util.Log

object AndroidThemeLogger : ThemeLogger {
    private const val TAG = "TemaTV"

    override fun info(message: String) {
        Log.i(TAG, message)
    }

    override fun warn(message: String) {
        Log.w(TAG, message)
    }
}
