# Onboarding: testar a troca da tela inicial (feature 003)

> Pré-requisitos: ferramentas da feature 001 e `local.properties` com o `sdk.dir`.

## 1. Compilar e testar

```bash
cd launcher/android
./gradlew :theme-core:test :app:testDebugUnitTest :app:assembleDebug
```

## 2. Instalar e ligar o serviço na TV

> **Antes de começar: ligue a TV (TCL, 192.168.1.2), deixe-a na tela inicial e confirme que ela responde (`ping -c 2 192.168.1.2`).**

```bash
export ANDROID_SERIAL=192.168.1.2:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
# ligar o serviço preservando os já ligados (ver interfaces/ativacao-acessibilidade-adb.md)
SERVICE=br.tematv.launcher/br.tematv.launcher.takeover.HomeTakeoverService
adb shell settings put secure enabled_accessibility_services "$SERVICE"   # na TCL de referência a lista estava vazia
adb shell settings put secure accessibility_enabled 1
adb logcat -d -s TemaTV | tail -5
```

## 3. Conferir

- Abrir o YouTube e apertar Home: a tela temática volta em até 1,5 s (`adb shell input keyevent KEYCODE_HOME`).
- Na tela "Tema": segurar OK 3 s em "Voltar ao launcher original do Google" leva ao Google; Home continua no Google; "Usar o tema como tela inicial" religa.
- Desligar o serviço (`adb shell settings put secure enabled_accessibility_services null`) e abrir o launcher: aparece a faixa discreta no rodapé.

## 4. Teste acelerado de 30 ciclos

```bash
tools/takeover_cycle_test.sh 192.168.1.2:5555 15 15   # 15 reinicializações + 15 saídas do modo de espera
```

O script registra cada ciclo e termina com o total de sucessos e falhas.

## 5. Desfazer

```bash
adb shell settings put secure enabled_accessibility_services null
adb shell settings put secure accessibility_enabled 0
adb uninstall br.tematv.launcher
```
