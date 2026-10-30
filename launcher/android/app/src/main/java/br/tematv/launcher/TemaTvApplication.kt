package br.tematv.launcher

import android.app.Application
import br.tematv.launcher.theme.AndroidThemeLogger
import br.tematv.launcher.theme.DefaultThemeProvider
import br.tematv.launcher.theme.SharedPreferencesThemeState
import br.tematv.launcher.theme.ThemeRepository
import br.tematv.launcher.theme.ThemeStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TemaTvApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var themeRepository: ThemeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        // A pasta externa só existe depois que o app a pede; o ADB da instalação depende dela.
        val themeStorage = ThemeStorage.fromContext(this)
        themeStorage.ensureThemesDir()

        themeRepository = ThemeRepository(
            packageStore = themeStorage,
            themeState = SharedPreferencesThemeState(this),
            defaultThemeSource = DefaultThemeProvider(this),
            logger = AndroidThemeLogger,
            ioDispatcher = Dispatchers.IO,
        )
        applicationScope.launch { themeRepository.reload() }
    }
}
