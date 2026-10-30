# Interface: Estado do app pela importação do ADB

> Identificador da feature: `006-device-provisioning`
> Tipo: ContentProvider (amplia `_reversa_forward/001-theme-engine/interfaces/importacao-adb.md`)
> Produtor: app `br.tematv.launcher`
> Consumidor: aba "Instalar na TV" (`:theme-builder`)

## Request

```bash
adb -s <ip>:5555 shell "content read --uri content://br.tematv.launcher.themes/app_status.json"
```

## Response

```json
{"version_code":6,"version_name":"0.6.0","signer_sha256":"ab12…","activated":true,"active_theme_id":"igreja-batista-x","updated_at":"2026-10-30T20:00:00Z"}
```

Campos em `_reversa_forward/006-device-provisioning/data-delta.md` §1.

## Erros

| Situação | Resultado |
|----------|-----------|
| App ainda não iniciado | Arquivo inexistente: enviar a recarga e ler de novo |
| App de versão anterior a esta feature | O nome não é aceito pela importação: tratar como "estado desconhecido" (instalar) |

## Idempotência

Leitura sem efeito colateral. O app regrava o arquivo a cada recarga.

## Segurança

Só o ADB (mesma regra da importação). O arquivo não contém a senha nem o código de ativação.
