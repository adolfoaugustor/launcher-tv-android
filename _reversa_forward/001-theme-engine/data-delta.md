# Data delta: Motor de temas (theme-engine)

> Identificador: `001-theme-engine`
> Data: `2026-10-29`
> Base: `_reversa_sdd/sdd/theme-engine.md#9-modelo-de-dados` (não há modelo legado)

## 1. Entidades novas

### 1.1 `theme.json` (manifesto do pacote, esquema v1) 🟢

| Campo | Tipo | Obrigatório | Regra de validação | Código de erro |
|-------|------|-------------|--------------------|----------------|
| `schema_version` | inteiro | sim | igual a 1 | `THEME_SCHEMA_UNSUPPORTED` |
| `theme_id` | texto | sim | kebab-case ASCII, 3 a 40 caracteres | `THEME_FIELD_INVALID: theme_id` |
| `display_name` | texto | sim | 1 a 60 caracteres | `THEME_FIELD_INVALID: display_name` |
| `colors.background` | texto | sim | `#RRGGBB` | `THEME_FIELD_INVALID: colors.background` |
| `colors.text` | texto | sim | `#RRGGBB` | `THEME_FIELD_INVALID: colors.text` |
| `colors.accent` | texto | sim | `#RRGGBB` | `THEME_FIELD_INVALID: colors.accent` |
| `background_image` | texto | não | arquivo existente no pacote; PNG, JPEG ou WEBP | `THEME_ASSET_MISSING` / `THEME_ASSET_INVALID` |
| `logo_image` | texto | sim | arquivo PNG existente no pacote | `THEME_ASSET_MISSING` / `THEME_ASSET_INVALID` |
| `message` | texto | sim | 1 a 120 caracteres | `THEME_FIELD_INVALID: message` |
| `font_file` | texto | não | arquivo `.ttf` ou `.otf` existente no pacote | `THEME_ASSET_MISSING` / `THEME_ASSET_INVALID` |
| `icon_frame.image` | texto | sim | arquivo PNG existente no pacote | `THEME_ASSET_MISSING` / `THEME_ASSET_INVALID` |
| `icon_frame.icon_slot` | objeto `{x, y, w, h}` | sim | números de 0 a 1; `x + w ≤ 1`; `y + h ≤ 1`; `w` e `h` > 0 | `THEME_FIELD_INVALID: icon_frame.icon_slot` |
| `icon_frame.label_slot` | objeto `{x, y, w, h}` | sim | mesmas regras do `icon_slot` | `THEME_FIELD_INVALID: icon_frame.label_slot` |
| `verses` | lista | sim | 1 a 366 itens | `THEME_FIELD_INVALID: verses` |
| `verses[i].reference` | texto | sim | 1 a 40 caracteres | `THEME_FIELD_INVALID: verses[i].reference` |
| `verses[i].text` | texto | sim | 1 a 600 caracteres | `THEME_FIELD_INVALID: verses[i].text` |
| `verses[i].version` | texto | sim | 2 a 10 caracteres | `THEME_FIELD_INVALID: verses[i].version` |
| `verses_attribution` | texto | sim | 1 a 120 caracteres | `THEME_FIELD_INVALID: verses_attribution` |

Campos desconhecidos no JSON são ignorados (permite evolução sem quebrar o v1). JSON malformado gera `THEME_MANIFEST_INVALID`.

### 1.2 Limites do arquivo `.tematv` 🟢

| Limite | Valor | Código de erro |
|--------|-------|----------------|
| Tamanho do arquivo compactado | ≤ 15 MB | `THEME_TOO_LARGE` |
| Total descompactado | ≤ 40 MB | `THEME_ARCHIVE_UNSAFE` |
| Número de entradas | ≤ 200 | `THEME_ARCHIVE_UNSAFE` |
| Caminho de entrada | sem `..`, sem `/` inicial | `THEME_ARCHIVE_UNSAFE` |
| Integridade do ZIP | legível do início ao fim | `THEME_ARCHIVE_CORRUPT` |

### 1.3 Estado persistido no app (`SharedPreferences` chamado `theme_state`) 🟢

| Chave | Tipo | Conteúdo |
|-------|------|----------|
| `last_valid_package` | texto ou ausente | Nome do arquivo `.tematv` do último tema aplicado com sucesso |
| `last_valid_cache_dir` | texto ou ausente | Pasta descompactada desse tema em `theme-cache/` |
| `last_error_code` | texto ou ausente | Código do último erro, por exemplo `THEME_FIELD_INVALID` |
| `last_error_field` | texto ou ausente | Campo do último erro, por exemplo `colors.text` |
| `last_error_package` | texto ou ausente | Pacote que causou o erro |
| `last_error_at` | texto ISO 8601 ou ausente | Data e hora do erro |

### 1.4 Layout de pastas na TV 🟢

```
Android/data/br.tematv.launcher/files/themes/     (pasta externa do app, gravável pelo ADB)
  active_theme.txt                                 nome do pacote ativo, uma linha, UTF-8
  igreja-x.tematv
  igreja-y.tematv                                  pacotes anteriores ficam guardados (RN-05)

/data/data/br.tematv.launcher/files/theme-cache/   (pasta interna, privada)
  igreja-x-<sha256 curto de 12 caracteres>/        pacote descompactado e validado
```

O cache de um pacote é reaproveitado quando o SHA-256 do arquivo não mudou. Caches de pacotes que não são o ativo nem o último válido são apagados após uma troca bem-sucedida.

O tema padrão embutido não usa `theme-cache/`: ele é copiado de `assets/default-theme/` para `/data/data/br.tematv.launcher/files/default-theme-<versionCode>/` uma vez por versão do app (ajuste feito no `/reversa-coding`, para a limpeza de caches nunca apagar o tema padrão).

## 2. Campos removidos

n/a.

## 3. Migrações necessárias

n/a. Primeira versão do formato e do app. Mudanças futuras no formato incrementam `schema_version`.
