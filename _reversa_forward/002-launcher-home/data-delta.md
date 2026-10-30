# Data delta: Tela inicial do launcher (launcher-home)

> Identificador: `002-launcher-home`
> Data: `2026-10-30`
> Base: `_reversa_sdd/sdd/launcher-home.md#9-modelo-de-dados` e `_reversa_sdd/addenda/001-theme-engine.md`

## 1. Entidades novas

### 1.1 `launcher_prefs` (`SharedPreferences`) 🟢

| Chave | Tipo | Conteúdo |
|-------|------|----------|
| `last_focused_package` | texto ou ausente | Pacote do último app aberto pela grade, para devolver o foco (RF-13) |

### 1.2 `AppEntry` (em memória) 🟢

| Campo | Tipo | Conteúdo |
|-------|------|----------|
| `packageName` | texto | Pacote do app |
| `activityName` | texto | Atividade de lançador de TV |
| `label` | texto | Nome exibido |
| `artwork` | imagem | Banner de TV, ou ícone quando não há banner |
| `hasBanner` | booleano | Se `artwork` é banner (para o modo de encaixe) |

### 1.3 `HiddenSystemApps` (constante no código) 🟢

`com.tcl.guard`, `com.tcl.suspension`, `com.tcl.partnercustomizer`, `com.tcl.notereminder`, `com.tcl.dashboard`, `com.tcl.waterfall.overseas`, `com.tcl.t_solo`.

## 2. Campos removidos

n/a.

## 3. Migrações necessárias

n/a. O formato `.tematv`, o `theme_state` e as pastas da feature 001 não mudam.
