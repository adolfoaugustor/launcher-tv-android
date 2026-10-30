# Interface: API da aba "Instalar na TV"

> Identificador da feature: `006-device-provisioning`
> Tipo: HTTP local (mesmas regras de `_reversa_forward/005-theme-builder/interfaces/api-local.md`)

## Rotas

| Método e caminho | Request | Response |
|------------------|---------|----------|
| `GET /api/instalacao/ambiente` | | `{adb_available, adb_install_command, launcher: {version_name, version_code} \| null, launcher_missing_hint}` |
| `POST /api/instalacao/verificar` | `{ip}` | `{found, message}`; `400` IP inválido |
| `POST /api/instalacao/etapas/<etapa>` | `{install_id, ip, theme_id, device_code?}` | `{status: "ok"\|"skipped"\|"error", message, error_code?, device_code?}` |
| `POST /api/instalacao/desfazer` | `{install_id, ip, theme_id}` | `{status, message}` |

Etapas, em ordem: `conectar`, `instalar`, `inicio-automatico`, `codigo`, `tema`, `ativacao`, `acessibilidade`, `conferencia`. A etapa `codigo` devolve `device_code`, que a página repassa à `ativacao`. A `instalar` devolve `error_code: "assinatura-diferente"` quando a TV tem o launcher assinado com outra chave.

## Erros

`400` para IP ou cliente inválido (cliente sem pacote: `field: "theme_id"`). Falhas na TV não são erro HTTP: voltam com `status: "error"` e a mensagem para a lista de verificação.

## Idempotência

Cada etapa pode ser repetida; a de acessibilidade não duplica o serviço; a de instalação pula se versão e assinatura forem iguais.

## Timeouts

`conectar`: até 120 s (autorização na TV). `codigo` e `tema`: até 30 s de repetição da importação. `acessibilidade`: até 15 s. `conferencia`: 5 s após o serviço ligado. Demais comandos: 60 s (instalação do APK: 180 s).

## Segurança

A senha de ativação é gerada e enviada dentro do servidor; não volta para a página nem vai para o registro.
