# Interface: Ligar o serviço de acessibilidade pelo ADB

> Identificador da feature: `003-home-takeover`
> Tipo: configuração segura do Android (`settings put secure`)
> Produtor: instalador no notebook, pelo ADB (feature 006) e testes manuais
> Consumidor: Android (vincula o `HomeTakeoverService`)

## Request

```
Componente do serviço: br.tematv.launcher/br.tematv.launcher.takeover.HomeTakeoverService
Configurações:         secure enabled_accessibility_services  (lista separada por ":")
                       secure accessibility_enabled            (1)
```

O instalador **acrescenta** o componente à lista existente, sem remover os serviços já ligados:

```bash
SERVICE=br.tematv.launcher/br.tematv.launcher.takeover.HomeTakeoverService
CURRENT=$(adb -s <ip>:5555 shell settings get secure enabled_accessibility_services)
case "$CURRENT" in
  null|"") NEW="$SERVICE" ;;
  *"$SERVICE"*) NEW="$CURRENT" ;;
  *) NEW="$CURRENT:$SERVICE" ;;
esac
adb -s <ip>:5555 shell settings put secure enabled_accessibility_services "$NEW"
adb -s <ip>:5555 shell settings put secure accessibility_enabled 1
```

## Response

- O Android vincula o serviço em segundos; o log `TemaTV` registra `Troca da tela inicial: serviço conectado`.
- Se a TV ligou há menos de 3 minutos e a troca está ligada, a tela temática abre logo em seguida.

## Desligar (desfazer)

Remover o componente da lista, mantendo os demais; se a lista ficar vazia, gravar `accessibility_enabled 0`.

## Erros

- O comando `settings` não devolve erro se o componente não existir; o instalador confere com `dumpsys accessibility` que o serviço aparece como ligado.

## Idempotência

Acrescentar um componente que já está na lista não o duplica (passo `case` acima).

## Segurança

Só o ADB autorizado ou o usuário nas Configurações liga o serviço. O serviço recebe apenas eventos de troca de janela dos launchers do sistema e não lê conteúdo de tela.
