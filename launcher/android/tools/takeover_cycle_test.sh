#!/usr/bin/env bash
# Teste acelerado da troca da tela inicial (feature 003, D-15).
# Reinicia a TV e simula o modo de espera, conferindo se a tela temática volta sozinha.
#
# Uso: tools/takeover_cycle_test.sh <ip:porta> <reinicializações> <ciclos de espera>
# Exemplo: tools/takeover_cycle_test.sh 192.168.1.2:5555 15 15
set -u

SERIAL=${1:?informe ip:porta da TV}
REBOOTS=${2:?informe o número de reinicializações}
STANDBYS=${3:?informe o número de ciclos de modo de espera}
HOME_ACTIVITY="br.tematv.launcher/br.tematv.launcher.home.HomeActivity"
OTHER_APP="com.google.android.youtube.tv"
BOOT_TIMEOUT_S=240
FOCUS_TIMEOUT_S=60

adb_tv() { adb -s "$SERIAL" "$@"; }

# A TCL lista mais de uma tela depois do modo de espera; a linha da tela ativa é a que não está vazia.
focused_window() { adb_tv shell dumpsys window 2>/dev/null | grep mCurrentFocus | grep -v '=null' | head -1 | sed 's/.*u0 //;s/}//' | tr -d '\r'; }

# Espera a TV voltar à rede e terminar o boot; devolve 1 se passar do tempo.
wait_for_boot() {
    local deadline=$((SECONDS + BOOT_TIMEOUT_S))
    while [ $SECONDS -lt $deadline ]; do
        adb connect "$SERIAL" >/dev/null 2>&1
        if [ "$(adb_tv shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then return 0; fi
        sleep 3
    done
    return 1
}

# Espera a tela temática ficar em primeiro plano; imprime os segundos gastos ou "falha:<janela>".
wait_for_themed_home() {
    local started=$SECONDS window=""
    while [ $((SECONDS - started)) -lt $FOCUS_TIMEOUT_S ]; do
        window=$(focused_window)
        if [ "$window" = "$HOME_ACTIVITY" ]; then echo "$((SECONDS - started))"; return 0; fi
        sleep 1
    done
    echo "falha:${window:-sem janela}"
    return 1
}

successes=0
failures=0
cycle=0

for _ in $(seq "$REBOOTS"); do
    cycle=$((cycle + 1))
    adb_tv reboot >/dev/null 2>&1
    sleep 20
    if ! wait_for_boot; then
        echo "ciclo $cycle reinício: FALHA (a TV não voltou em ${BOOT_TIMEOUT_S}s)"
        failures=$((failures + 1))
        continue
    fi
    if result=$(wait_for_themed_home); then
        echo "ciclo $cycle reinício: ok (tela temática ${result}s após o fim do boot)"
        successes=$((successes + 1))
    else
        echo "ciclo $cycle reinício: FALHA ($result)"
        failures=$((failures + 1))
    fi
done

for _ in $(seq "$STANDBYS"); do
    cycle=$((cycle + 1))
    adb_tv shell monkey -p "$OTHER_APP" -c android.intent.category.LEANBACK_LAUNCHER 1 >/dev/null 2>&1
    sleep 4
    adb_tv shell input keyevent KEYCODE_SLEEP
    sleep 8
    adb connect "$SERIAL" >/dev/null 2>&1
    adb_tv shell input keyevent KEYCODE_WAKEUP
    sleep 5
    # Ao acordar, a TV pode voltar direto ao último app (sem troca, D-06); nesse caso o ciclo confere o Home.
    window_after_wake=$(focused_window)
    if [ "$window_after_wake" = "$HOME_ACTIVITY" ]; then
        echo "ciclo $cycle espera: ok (tela temática ao acordar)"
        successes=$((successes + 1))
        continue
    fi
    adb_tv shell input keyevent KEYCODE_HOME
    if result=$(wait_for_themed_home); then
        echo "ciclo $cycle espera: ok (acordou em ${window_after_wake:-sem janela}; tela temática ${result}s após Home)"
        successes=$((successes + 1))
    else
        echo "ciclo $cycle espera: FALHA ($result)"
        failures=$((failures + 1))
    fi
done

echo "total: $successes ok, $failures falhas em $cycle ciclos"
[ "$failures" -eq 0 ]
