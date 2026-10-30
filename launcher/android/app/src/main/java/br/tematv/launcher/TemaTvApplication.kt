package br.tematv.launcher

import android.app.Application
import br.tematv.launcher.activation.ActivationService
import br.tematv.launcher.theme.AndroidThemeLogger
import br.tematv.launcher.theme.DefaultThemeProvider
import br.tematv.launcher.theme.SharedPreferencesThemeState
import br.tematv.launcher.theme.ThemeRepository
import br.tematv.launcher.theme.ThemeStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TemaTvApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var themeRepository: ThemeRepository
        private set

    lateinit var activationService: ActivationService
        private set

    private lateinit var themeStorage: ThemeStorage

    override fun onCreate() {
        super.onCreate()
        // A pasta externa só existe depois que o app a pede; o ADB da instalação depende dela.
        themeStorage = ThemeStorage.fromContext(this)
        themeStorage.ensureThemesDir()
        activationService = ActivationService(this)

        themeRepository = ThemeRepository(
            packageStore = themeStorage,
            themeState = SharedPreferencesThemeState(this),
            defaultThemeSource = DefaultThemeProvider(this),
            logger = AndroidThemeLogger,
            ioDispatcher = Dispatchers.IO,
        )
        applicationScope.launch { reloadImports() }
    }

    /** Lê o que o ADB enviou pela importação: tema ativo e senha de ativação; publica o código do aparelho. */
    suspend fun reloadImports() {
        withContext(Dispatchers.IO) {
            activationService.publishDeviceCode(themeStorage)
            activationService.importActivationCode(themeStorage)
        }
        themeRepository.reload()
    }
}
