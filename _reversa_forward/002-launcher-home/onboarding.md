# Onboarding: testar a tela inicial (feature 002)

> Pré-requisitos: ferramentas da feature 001 (`_reversa_forward/001-theme-engine/onboarding.md` §1) e `local.properties` com o `sdk.dir`.

## 1. Compilar e testar

```bash
cd launcher/android
./gradlew :theme-core:test :app:testDebugUnitTest :app:assembleDebug :app:packSampleThemes
```

## 2. Testar na TV de referência

> **Antes de começar: ligue a TV (TCL, 192.168.1.2), deixe-a na tela inicial e confirme que ela responde (`ping -c 2 192.168.1.2`).** Se aparecer "Permitir depuração?", aceite.

```bash
export ANDROID_SERIAL=192.168.1.2:5555
adb connect $ANDROID_SERIAL
adb install -r app/build/outputs/apk/debug/app-debug.apk
I=content://br.tematv.launcher.themes
adb shell "content write --uri $I/biblia-exemplo.tematv" < app/build/sample-themes/biblia-exemplo.tematv
echo biblia-exemplo.tematv | adb shell "content write --uri $I/active_theme.txt"
adb shell am start -W -n br.tematv.launcher/.home.HomeActivity
adb exec-out screencap -p > tela-biblia.png
```

Conferir na TV ou na captura:
- Fundo, logotipo e mensagem do tema; versículo do dia com referência, sigla e atribuição.
- Apps dentro da moldura de Bíblia, em ordem alfabética, sem os utilitários "Guard", "Suspension" etc.
- Botões "Configurações" e "Tema" no canto superior direito (direcional para cima a partir da primeira linha).
- OK abre o app em foco; Voltar na grade não sai da tela.
- "Tema" mostra o diagnóstico.

Troca de tema com a tela aberta:

```bash
adb shell "content write --uri $I/hamburguer-exemplo.tematv" < app/build/sample-themes/hamburguer-exemplo.tematv
echo hamburguer-exemplo.tematv | adb shell "content write --uri $I/active_theme.txt"
adb shell am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver
```

## 3. Medidas

```bash
adb shell am force-stop br.tematv.launcher
adb shell am start -W -n br.tematv.launcher/.home.HomeActivity | grep TotalTime   # meta: ≤ 2000 ms
adb shell dumpsys meminfo br.tematv.launcher | grep "TOTAL PSS"                       # meta: ≤ 100 MB
adb logcat -d -s TemaTV | grep "Tema aplicado"                                         # tempo de aplicação; meta: ≤ 3000 ms
```

## 4. Desfazer

```bash
adb uninstall br.tematv.launcher
```
