# Data delta: Ativação por licença (license-activation)

> Identificador: `004-license-activation`
> Data: `2026-10-30`
> Base: `_reversa_sdd/sdd/license-activation.md#9-modelo-de-dados` (o arquivo `license.key` da spec foi trocado pela senha de 12 números)

## 1. Entidades novas

### 1.1 `activation_prefs` (na TV, `SharedPreferences`) 🟢

| Chave | Tipo | Conteúdo |
|-------|------|----------|
| `activation_code` | texto (12 dígitos) ou ausente | Última senha aceita |
| `activated_at` | texto ISO 8601 ou ausente | Data e hora da ativação |
| `failed_attempts` | inteiro | Erros seguidos (RN-05) |
| `locked_until_elapsed_ms` | inteiro longo | Fim da espera de 30 s, em `elapsedRealtime` |

A ativação vale enquanto `activation_code` for igual à senha esperada para o código do aparelho atual.

### 1.2 Arquivos na pasta de importação (na TV) 🟢

| Arquivo | Quem grava | Quem lê | Conteúdo |
|---------|------------|---------|----------|
| `device_code.txt` | o app, ao iniciar | o ADB (`content read`) | `XXXX-XXXX-XXXX` |
| `activation_code.txt` | o ADB (`content write`) | o app, na recarga; apagado depois | senha, com ou sem traços |

### 1.3 Segredo de ativação (no notebook) 🟢

`~/.config/tematv/activation-secret`, permissão `600`: 64 caracteres hexadecimais (32 bytes aleatórios), uma linha.

### 1.4 Registro de senhas (no notebook) 🟢

`~/tematv/ativacoes.csv`, uma linha por senha gerada: `data_iso8601,codigo_do_aparelho,cliente`. A senha não é gravada no registro (pode ser gerada de novo a partir do código).

## 2. Campos removidos

O arquivo `license.key` previsto em `_reversa_sdd/sdd/license-activation.md#9-modelo-de-dados` não existe (sem `customer`, `issued_at`, `expires_at` nem `signature`).

## 3. Migrações necessárias

n/a. TVs já instaladas passam a pedir ativação após a atualização.
