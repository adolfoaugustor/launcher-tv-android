# Onboarding: testar o motor de temas (feature 001)

> Para quem vai compilar e testar a feature pela primeira vez, no Linux ou no WSL.

## 1. Instalar as ferramentas (uma vez por computador)

1. Java 17:
   ```bash
   sudo apt install -y openjdk-17-jdk unzip
   ```
2. Android SDK (linha de comando). Baixe "Command line tools only" para Linux em https://developer.android.com/studio#command-line-tools-only e rode:
   ```bash
   mkdir -p ~/Android/Sdk/cmdline-tools
   unzip ~/Downloads/commandlinetools-linux-*_latest.zip -d ~/Android/Sdk/cmdline-tools
   mv ~/Android/Sdk/cmdline-tools/cmdline-tools ~/Android/Sdk/cmdline-tools/latest
   ```
   No WSL, o arquivo baixado pelo navegador do Windows fica em `/mnt/c/Users/<seu usuário>/Downloads/`.
3. Variáveis de ambiente (acrescente ao `~/.zshrc`):
   ```bash
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
   ```
4. Pacotes do SDK e licenças:
   ```bash
   sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
   yes | sdkmanager --licenses
   ```
5. Gradle 8.7, só para gerar o wrapper do projeto uma vez. Baixe o zip "binary-only" em https://gradle.org/releases/, descompacte em `~/gradle` e rode `~/gradle/gradle-8.7/bin/gradle --version`.

## 2. Compilar e rodar os testes

```bash
cd launcher/android
echo "sdk.dir=$HOME/Android/Sdk" > local.properties   # só na primeira vez; o wrapper 8.7 já está no projeto
./gradlew :theme-core:test :app:testDebugUnitTest
./gradlew :app:assembleDebug :app:packSampleThemes
```

Resultado esperado:
- Os testes passam.
- O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.
- Os pacotes `biblia-exemplo.tematv` e `hamburguer-exemplo.tematv` ficam em `app/build/sample-themes/`.

## 3. Testar na TV de referência

> **Antes de começar: ligue a TV (TCL, 192.168.1.2), deixe-a na tela inicial e confirme que ela responde:**
> ```bash
> ping -c 2 192.168.1.2
> ```
> Se o `adb` pedir autorização, aceite o aviso "Permitir depuração?" na tela da TV.

```bash
export ANDROID_SERIAL=192.168.1.2:5555
adb connect $ANDROID_SERIAL
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver
```

Esse primeiro broadcast inicia o app, que cria a pasta `themes/`. O log deve mostrar o tema padrão:

```bash
adb logcat -d -s TemaTV
```

Aplicar o pacote de exemplo:

O ADB não tem permissão para gravar na pasta do app; os arquivos entram pela importação do ADB (`interfaces/importacao-adb.md`):

```bash
I=content://br.tematv.launcher.themes
adb shell "content write --uri $I/biblia-exemplo.tematv" < app/build/sample-themes/biblia-exemplo.tematv
echo biblia-exemplo.tematv | adb shell "content write --uri $I/active_theme.txt"
adb shell am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver
adb logcat -d -s TemaTV | tail -5
```

Esperado: `Tema aplicado: biblia-exemplo`.

Testar a recusa de pacote inválido (mantém o último tema válido):

```bash
echo lixo | adb shell "content write --uri $I/quebrado.tematv"
echo quebrado.tematv | adb shell "content write --uri $I/active_theme.txt"
adb shell am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver
adb logcat -d -s TemaTV | tail -3
```

Esperado: `Pacote recusado: THEME_ARCHIVE_CORRUPT (quebrado.tematv) ...` seguido de `Tema aplicado: biblia-exemplo`.

Testar pacote ativo inexistente (usa o tema padrão):

```bash
echo apagado.tematv | adb shell "content write --uri $I/active_theme.txt"
adb shell am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver
adb logcat -d -s TemaTV | tail -3
```

Esperado: `Pacote recusado: THEME_ACTIVE_NOT_FOUND (apagado.tematv)` seguido de `Tema aplicado: padrao-biblico (padrão)`.

## 4. Desfazer

```bash
adb uninstall br.tematv.launcher
```

O app desta feature não aparece na tela da TV e não altera o launcher do Google.
