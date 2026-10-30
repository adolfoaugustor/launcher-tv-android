# Interface: Ativação pelo ADB

> Identificador da feature: `004-license-activation`
> Tipo: ContentProvider (amplia a importação de `_reversa_forward/001-theme-engine/interfaces/importacao-adb.md`)
> Produtor e consumidor: instalador no notebook (feature 006) e testes manuais

## Request

```bash
I=content://br.tematv.launcher.themes
CODIGO=$(adb -s <ip>:5555 shell "content read --uri $I/device_code.txt")
SENHA=$(launcher/android/activation-tool/build/install/activation-tool/bin/activation-tool senha "$CODIGO" --cliente "Igreja X" | tail -1)
echo "$SENHA" | adb -s <ip>:5555 shell "content write --uri $I/activation_code.txt"
adb -s <ip>:5555 shell am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver
```

## Response

- Na recarga, o app lê `activation_code.txt`, apaga o arquivo e, se a senha corresponder ao código do aparelho, grava a ativação; o log `TemaTV` registra `Ativação: aceita` ou `Ativação: senha recusada`.
- A tela inicial passa da ativação para a grade temática na próxima vez que ficar visível.

## Erros

| Situação | Resultado |
|----------|-----------|
| `device_code.txt` ainda não existe | `content read` falha: abrir o app uma vez antes (ele grava o arquivo ao iniciar) |
| Senha errada | Log `Ativação: senha recusada`; a TV continua sem ativação; conta como erro para a espera de 30 s |
| Chamador que não é o ADB | `SecurityException` (mesma regra da importação) |

## Idempotência

Enviar a mesma senha de novo mantém a TV ativada.

## Timeouts

Nenhum próprio; após `adb install -r`, repetir o `content write` até o provider responder (observado na feature 002).
