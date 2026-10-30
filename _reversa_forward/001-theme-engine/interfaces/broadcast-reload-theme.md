# Interface: Broadcast de recarga do tema

> Identificador da feature: `001-theme-engine`
> Tipo: intent Android (broadcast explícito)
> Produtor: feature 6 (`device-provisioning`) pelo ADB; testes manuais
> Consumidor: `ReloadThemeReceiver` no `:app`

## Request

```
Ação:       br.tematv.launcher.action.RELOAD_THEME
Componente: br.tematv.launcher/.theme.ReloadThemeReceiver
Extras:     nenhum (extras recebidos são ignorados)
Flags:      FLAG_INCLUDE_STOPPED_PACKAGES (-f 32), porque o app recém-instalado está "parado" e não recebe broadcasts sem ela
```

Pelo ADB:

```bash
adb -s <ip>:5555 shell am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver
```

## Response

- O receiver chama `ThemeRepository.reload()` em segundo plano e devolve imediatamente.
- O resultado não volta no broadcast; ele aparece no log `TemaTV` (`Tema aplicado: <theme_id>` ou `Pacote recusado: <código> <campo>`) e no estado `theme_state`.

## Erros

- Nenhum erro é devolvido ao emissor. Falhas de recarga seguem as regras de `pacote-tematv.md` (tema anterior mantido e erro registrado).

## Idempotência

Vários broadcasts seguidos produzem o mesmo estado final. Uma recarga em andamento faz as seguintes esperarem a anterior terminar, sem rodar em paralelo.

## Segurança

O receiver é exportado sem permissão para o ADB conseguir chamá-lo. O impacto de outro app disparar o broadcast é só uma recarga do tema já instalado, porque o intent não carrega dados.

## Timeouts

A recarga precisa terminar em até 3 s para um pacote de até 15 MB. O receiver usa `goAsync()` e encerra em até 10 s, o limite do Android para broadcasts.
