# Data delta: Instalação na TV (device-provisioning)

> Identificador: `006-device-provisioning`
> Data: `2026-10-30`
> Base: `_reversa_sdd/sdd/device-provisioning.md#9-modelo-de-dados`

## 1. Na TV: `app_status.json` (pasta de importação, só leitura pelo ADB)

| Campo | Tipo | Conteúdo |
|-------|------|----------|
| `version_code` | inteiro | `versionCode` instalado |
| `version_name` | texto | `versionName` instalado |
| `signer_sha256` | texto | SHA-256 em hexadecimal minúsculo do certificado de assinatura |
| `activated` | booleano | resultado de `ActivationService.isActivated()` |
| `active_theme_id` | texto ou `null` | `theme_id` do tema aplicado (`null` = tema padrão) |
| `updated_at` | texto ISO 8601 | momento da gravação |

Gravado ao iniciar o app e a cada recarga, depois de importar a senha e o tema.

## 2. No notebook

| Arquivo | Conteúdo | Permissão |
|---------|----------|-----------|
| `~/.config/tematv/assinatura.jks` | chave definitiva (PKCS12) | `600` |
| `~/.config/tematv/assinatura.properties` | `storeFile`, `storePassword`, `keyAlias`, `keyPassword` | `600` |
| `~/tematv/launcher/tematv-launcher.apk` | APK de produção | padrão |
| `~/tematv/launcher/launcher.json` | `version_code`, `version_name`, `signer_sha256` | padrão |
| `~/tematv/instalacoes/<AAAA-MM-DD>-<ip>.log` | uma linha JSON por etapa | padrão |

### 2.1 Linha do registro de instalação

| Campo | Conteúdo |
|-------|----------|
| `timestamp` | momento da etapa |
| `install_id` | identificador da tentativa (da página) |
| `client_theme_id`, `tv_ip` | cliente e TV |
| `tv_manufacturer`, `tv_model`, `android_version` | lidos na etapa de conexão |
| `device_code` | quando conhecido |
| `step` | `encontrar`, `conectar`, `instalar`, `inicio-automatico`, `codigo`, `tema`, `ativacao`, `acessibilidade`, `conferencia`, `desfazer` |
| `result` | `ok`, `skipped`, `error` |
| `detail` | mensagem |
| `elapsed_ms` | duração da etapa |

Nunca contém a senha nem o segredo (RN-05).

## 3. Removido em relação à spec

| Item | Motivo |
|------|--------|
| `step: license` e `license.key` | A licença é a senha de ativação (feature 004) |
| `step: port` separado de `connect` | Unidos em `encontrar` (porta 5555) |
| Kit de pendrive | Fora desta versão (esclarecimento) |

## 4. Migrações

Nenhuma. A TV de teste troca de assinatura uma vez (desfazer e instalar de novo).
