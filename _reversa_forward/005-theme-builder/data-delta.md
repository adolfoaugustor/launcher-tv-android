# Data delta: Ferramenta de preparo de tema (theme-builder)

> Identificador: `005-theme-builder`
> Data: `2026-10-30`
> Base: `_reversa_sdd/sdd/theme-builder.md#9-modelo-de-dados`

## 1. Entidades novas

### 1.1 `ClientProject` (`~/tematv/clientes/<theme_id>/projeto.json`)

| Campo | Tipo | Regra |
|-------|------|-------|
| `schema_version` | inteiro | 1 |
| `type` | `"igreja"` \| `"loja"` | definido ao criar; não muda |
| `theme_id` | texto | kebab-case ASCII, 3 a 40; definido ao criar; nomeia a pasta e o pacote |
| `display_name` | texto | 1 a 60 |
| `colors` | `{background, text, accent}` | `#RRGGBB` |
| `message` | texto | 1 a 120 |
| `logo_file` | texto | nome do PNG na pasta do projeto |
| `background_file` | texto ou ausente | PNG, JPEG ou WEBP na pasta do projeto |
| `icon_frame` | `{preset: "biblia" \| "hamburguer"}` ou `{image, icon_slot, label_slot}` | áreas com as regras do `theme.json` v1 |
| `verses` | lista de `{reference, text}` | 1 a 366; referência 1 a 40; texto 1 a 600; a versão não é guardada: vem do tipo (`NVI` ou `LOJA`) |
| `verses_attribution` | texto ou ausente | só na loja, 1 a 120; na igreja é sempre a atribuição da NVI |
| `updated_at` | data e hora ISO 8601 | atualizado a cada gravação |

Campos desconhecidos são ignorados na leitura.

### 1.2 Arquivos em `~/tematv/`

| Caminho | Conteúdo |
|---------|----------|
| `clientes/<theme_id>/` | `projeto.json`, `logo.png`, `fundo.<ext>` (opcional), `moldura.png` (só moldura própria) |
| `temas/<theme_id>.tematv` | último pacote gerado e validado |
| `ativacoes.csv` | mesmo arquivo e formato da feature 004: `instante,código,cliente` |

## 2. Removido em relação à spec

| Item da spec | Motivo |
|--------------|--------|
| `ToolConfig` (`~/.config/tematv/config.json` com `biblia_api_key`, `biblia_api_base_url`) | Sem Bíblia API (RN-02) |
| `ClientProject.bible_version` | Igreja é sempre NVI (RN-07) |
| `ClientProject.license_confirmed` | Sem escolha de versão; atribuição da NVI fixa |
| `verses[].source` (`api` / `manual`) | Não há API; todo item é da lista pronta ou digitado |

## 3. Pacote gerado

Sem mudança: `theme.json` v1 de `_reversa_forward/001-theme-engine/data-delta.md` §1.1. Mapeamento: `type=igreja` → `version: "NVI"` e atribuição da NVI; `type=loja` → `version: "LOJA"` e a atribuição do projeto. Arquivos no pacote: `logo.png`, `fundo.<ext>`, e a moldura com o nome do arquivo de origem (`frame-biblia.png`, `frame-hamburguer.png` ou `moldura.png`).

## 4. Migrações

Nenhuma.
